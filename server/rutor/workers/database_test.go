package workers

import (
	"context"
	"github.com/alecthomas/assert/v2"
	bolt "go.etcd.io/bbolt"
	"os"
	"path/filepath"
	"server/rutor/models"
	"testing"
	"time"
)

func TestWriteToDatabase(t *testing.T) {
	ctx := context.Background()
	ch := make(chan models.TorrentDetails, 10)
	hashes := []string{"hash1", "hash2", "hash3"}

	for _, v := range hashes {
		ch <- models.TorrentDetails{Hash: v}
	}
	close(ch)

	dbPath := filepath.Join(os.TempDir(), "torrents.db")
	db, err := bolt.Open(dbPath, 0666, &bolt.Options{Timeout: 5 * time.Second})
	if err != nil {
		t.Fatal(err)
	}
	defer func() {
		db.Close()
		os.Remove(dbPath)
	}()

	err = WriteToDatabase(ctx, db, 2, ch)
	assert.NoError(t, err)

	for _, k := range hashes {
		err = db.View(func(tx *bolt.Tx) error {
			val := tx.Bucket([]byte(BucketName)).Get([]byte(k))
			assert.True(t, val != nil)
			return nil
		})
	}
	assert.NoError(t, err)
}
