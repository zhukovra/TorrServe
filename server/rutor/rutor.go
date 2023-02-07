package rutor

import (
	"context"
	bolt "go.etcd.io/bbolt"
	"golang.org/x/sync/errgroup"
	"os"
	"path/filepath"
	"server/log"
	"server/rutor/models"
	"server/rutor/workers"
	"server/settings"
	"time"
)

var (
	dbPath    string
	indexPath string
)

func Start() {
	ticker := time.NewTicker(3 * time.Hour)
	ctx := context.Background()
	dbPath = filepath.Join(os.TempDir(), "torrents-db")
	indexPath = filepath.Join(os.TempDir(), "torrents-idx")

	go func() {
		if settings.BTsets.EnableRutorSearch {
			for ; true; <-ticker.C {
				func() {
					// TODO add mutex?
					updateCtx, cancel := context.WithTimeout(ctx, 5*time.Minute)
					defer cancel()
					if err := updateDB(updateCtx); err != nil {
						log.TLogln("Error updating rutor database: ", err)
						// TODO cleanup files
					}
				}()
			}
		}
		ticker.Stop()
	}()
}

func Stop() {
}

// https://github.com/yourok-0001/releases/raw/master/torr/rutor.ls
func updateDB(ctx context.Context) error {
	downloadChannel := make(chan models.TorrentDetails, 1000)
	dbChannel := make(chan models.TorrentDetails)
	indexChannel := make(chan models.TorrentDetails)

	// TODO check db version based on etag

	db, err := bolt.Open(dbPath, 0666, &bolt.Options{Timeout: 5 * time.Second})
	if err != nil {
		return err
	}

	index, err := workers.CreateIndex(indexPath)
	if err != nil {
		return err
	}

	// multiply messages in channels
	go func() {
		defer func() {
			close(dbChannel)
			close(indexChannel)
		}()
		for v := range downloadChannel {
			dbChannel <- v
			indexChannel <- v
		}
	}()

	group, ct := errgroup.WithContext(ctx)
	group.Go(func() error {
		return workers.Download(ct, "https://github.com/yourok-0001/releases/raw/master/torr/rutor.ls", downloadChannel)
	})
	group.Go(func() error {
		return workers.WriteToDatabase(ct, db, 10000, dbChannel)
	})
	group.Go(func() error {
		return workers.IndexWorker(ct, index, 1000, indexChannel)
	})
	if err := group.Wait(); err != nil {
		return err
	}
	log.TLogln("Complete building rutor database")
	return nil
}

func Search(query string) []*models.TorrentDetails {
	if !settings.BTsets.EnableRutorSearch {
		return nil
	}
	return nil
}
