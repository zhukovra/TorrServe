package workers

import (
	"compress/flate"
	"context"
	"encoding/json"
	"net/http"
	"server/log"
	"server/rutor/models"
)

// Download performs streaming on-the-fly download and unpack models.TorrentDetails to channel
// without unnecessary writing files to disk
func Download(ctx context.Context, url string, ch chan<- models.TorrentDetails) error {
	log.TLogln("Start download rutor database")

	resp, err := http.Get(url)
	if err != nil {
		log.TLogln("Error connect to rutor db:", err)
		return err
	}
	defer resp.Body.Close()

	jsonReader := flate.NewReader(resp.Body)
	dec := json.NewDecoder(jsonReader)

	// read open bracket
	_, err = dec.Token()
	if err != nil {
		log.TLogln("Error reading JSON tag [", err)
		return err
	}

out:
	for {
		select {
		case <-ctx.Done():
			// if context cancelled - return error
			return ctx.Err()
		default:
			if dec.More() {
				var m models.TorrentDetails
				err = dec.Decode(&m)
				if err != nil {
					log.TLogln("Error decoding TorrentDetails model: ", err)
					return err
				}
				ch <- m
			} else {
				break out
			}
		}
	}

	// read closing bracket
	_, err = dec.Token()
	if err != nil {
		log.TLogln("Error reading JSON tag ]", err)
		return err
	}

	log.TLogln("End download rutor database")
	return nil
}
