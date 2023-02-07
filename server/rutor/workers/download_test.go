package workers

import (
	"compress/flate"
	"context"
	"encoding/json"
	"github.com/alecthomas/assert/v2"
	"net/http"
	"net/http/httptest"
	"server/rutor/models"
	"testing"
)

func TestDownload(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, request *http.Request) {
		torrent := []models.TorrentDetails{
			{Hash: "hash1"},
			{Hash: "hash2"},
		}
		gzipWriter, _ := flate.NewWriter(w, -1)
		b, _ := json.Marshal(torrent)
		gzipWriter.Write(b)
		gzipWriter.Close()
	}))

	var ch = make(chan models.TorrentDetails, 2)
	var model models.TorrentDetails

	ctx := context.Background()
	err := Download(ctx, server.URL, ch)
	assert.NoError(t, err)

	model = <-ch
	assert.Equal(t, "hash1", model.Hash)
	model = <-ch
	assert.Equal(t, "hash2", model.Hash)
}
