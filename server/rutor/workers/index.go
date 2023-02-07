package workers

import (
	"github.com/blevesearch/bleve/v2"
	"github.com/blevesearch/bleve/v2/analysis/analyzer/web"
	"github.com/blevesearch/bleve/v2/analysis/lang/ru"
	"golang.org/x/net/context"
	"os"
	"server/rutor/models"
)

func IndexWorker(ctx context.Context, index bleve.Index, batchSize int, ch <-chan models.TorrentDetails) error {
	var batch = index.NewBatch()
	var err error

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case torr, ok := <-ch:
			if !ok {
				if batch.Size() > 0 {
					err = index.Batch(batch)
					if err != nil {
						return err
					}
				}
				return nil
			}
			err = batch.Index(torr.Hash, torr)
			if err != nil {
				return err
			}
			if batch.Size() >= batchSize {
				err = index.Batch(batch)
				if err != nil {
					return err
				}
				batch.Reset()
			}
		}
	}
}

func CreateIndex(indexPath string) (bleve.Index, error) {
	indexMapping := bleve.NewIndexMapping()
	torrMapping := bleve.NewDocumentStaticMapping()

	// a generic reusable mapping for english text
	titleField := bleve.NewTextFieldMapping()
	titleField.Analyzer = web.Name
	torrMapping.AddFieldMappingsAt("Title", titleField)

	// russian field
	nameField := bleve.NewTextFieldMapping()
	nameField.Analyzer = ru.AnalyzerName
	torrMapping.AddFieldMappingsAt("Name", nameField)

	indexMapping.AddDocumentMapping("torrent", torrMapping)
	if _, err := os.Stat(indexPath); err == nil {
		err := os.RemoveAll(indexPath)
		if err != nil {
			return nil, err
		}
	}
	index, err := bleve.New(indexPath, indexMapping)
	if err != nil {
		return nil, err
	}
	return index, nil
}
