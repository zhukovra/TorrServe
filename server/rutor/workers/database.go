package workers

import (
	"context"
	"github.com/goccy/go-json"
	bolt "go.etcd.io/bbolt"
	"server/rutor/models"
)

const BucketName = "torrents"

// WriteToDatabase performs batched write to database bucket (BucketName) from channel with models
func WriteToDatabase(ctx context.Context, db *bolt.DB, batchSize int, ch <-chan models.TorrentDetails) error {
	var batch []models.TorrentDetails
	var err error

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case model, ok := <-ch:
			if !ok {
				// write incomplete batch
				if len(batch) > 0 {
					err = writeBatch(db, batch)
					if err != nil {
						return err
					}
				}
				return nil
			}
			batch = append(batch, model)
			if len(batch) >= batchSize {
				err = writeBatch(db, batch)
				if err != nil {
					return err
				}
				batch = nil
			}
		}
	}
}

func writeBatch(db *bolt.DB, torrents []models.TorrentDetails) error {
	return db.Batch(func(tx *bolt.Tx) error {
		b, err := tx.CreateBucketIfNotExists([]byte(BucketName))
		if err != nil {
			return err
		}
		for _, v := range torrents {
			id := []byte(v.Hash)
			val, err := json.Marshal(v)
			if err != nil {
				return err
			}
			err = b.Put(id, val)
			if err != nil {
				return err
			}
		}
		return nil
	})
}
