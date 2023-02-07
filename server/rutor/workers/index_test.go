package workers

import (
	"context"
	"github.com/alecthomas/assert/v2"
	"os"
	"path/filepath"
	"server/rutor/models"
	"testing"
)

func TestIndexWorker(t *testing.T) {
	var err error
	var ctx = context.Background()
	indexPath := filepath.Join(os.TempDir(), "bleve-index")
	index, err := CreateIndex(indexPath)
	assert.NoError(t, err)
	defer func() {
		index.Close()
		os.Remove(indexPath)
	}()

	ch := make(chan models.TorrentDetails, 10)
	names := []string{"hash1", "hash2", "hash3"}

	for _, v := range names {
		ch <- models.TorrentDetails{Hash: v, Name: v}
	}
	close(ch)

	err = IndexWorker(ctx, index, 2, ch)
	assert.NoError(t, err)
	count, err := index.DocCount()
	assert.NoError(t, err)
	assert.Equal(t, 3, count, err)
}
