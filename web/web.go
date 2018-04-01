package web

import (
	"fmt"
	"log"
	"net/http"
	"io/ioutil"
	"data"
	"time"
	"math"
)
const page_header = "<!DOCTYPE html>" +
					"<html>" + 
					"<body>" +
					"<h2 align=\"center\">Incidents</h2>" +
					"<table style=\"width:100%\">" + 
					"<tr>" + 
					"<th>Time</th>" + 
					"<th>No.Sensors</th>" + 
					"<th>Appoximate Location</th>" +
					"</tr>"

const page_footer = "</table>" +
					"</body>" +
					"</html>"

const page_html = "<!DOCTYPE html>" +
				 "<html>" +
				 "<body>" +
				 "<h2 align=\"center\">Recent Incidents (%d)</h2>" +
				 "<table style=\"width:100%\">" + 
				 "<tr>" + 
				 "<th>Time</th>" + 
				 "<th>No.Sensors</th>" + 
				 "<th>Appoximate Location</th>" +
				 "</tr>" + 
				 "<tr>" +
				 "<td align=\"center\">14:45:66</td>" +
				 "<td align=\"center\">3</td>" +
				 "<td align=\"center\">(453,343)</td>" +
				 "</tr>" +
				 "</table>" +
				 "</body>" +
				 "</html>"

/* 
 ******************************************************************************
 *                                  Page
 ******************************************************************************
*/

// Basic webpage data structure.
type Page struct {
	Title string
	Body []byte
}

/* 
 ******************************************************************************
 *                                   Globals
 ******************************************************************************
*/

// Storage of events.
var events []data.Cluster

/* 
 ******************************************************************************
 *                              Page Methods
 ******************************************************************************
*/

// Saves a page body to text file.
func (p *Page) save() error {
	filename := p.Title + ".txt"
	return ioutil.WriteFile(filename, p.Body, 0600)
}

// Loads a page body from a text file.
func loadPage(title string) (*Page, error) {
	filename := title + ".txt"
	body, err := ioutil.ReadFile(filename)
	if err != nil {
		return nil, err
	}
	return &Page{Title: title, Body: body}, nil
}

/* 
 ******************************************************************************
 *                               Functions
 ******************************************************************************
*/

// Returns an x,y approximated origin of a signal.
func computeOrigin(cluster data.Cluster) (x float64, y float64) {
	var minTime int64 = math.MaxInt64
	var sum float64

	// Compute the minimum time.
	for _, g := range cluster.Members {
		minTime = int64(math.Min(float64(minTime), float64(g.When)))
	}

	// Compute the sum.
	for _, g := range cluster.Members {
		t := math.Max(0, 2 - float64(g.When - minTime) / 1000.0)
		x += g.Location[0] * t
		y += g.Location[1] * t
		sum += t
	}

	x = x / sum
	y = y / sum
	return
}

// Constructs a HTML table row string from a cluster.
func getClusterHTML(cluster data.Cluster) string {
	location,_ := time.LoadLocation("Europe/Amsterdam")
	x, y := computeOrigin(cluster)
	tm := time.Unix(cluster.Updated / 1000, 0).UTC().In(location)
	t := fmt.Sprintf("<td align=\"center\">%s</td>", tm.String())
	n := fmt.Sprintf("<td align=\"center\">%d</td>", len(cluster.Members))
	o := fmt.Sprintf("<td align=\"center\">(%d,%d)</td>", int64(x), int64(y))
	
	return "<tr>" + t + n + o + "</tr>"
}

// Constructs an HTML page from the events queue.
func getEventsPage() string {
	p := page_header
	for _, c := range events {
		p += getClusterHTML(c)
	}
	return p + page_footer
}


// Handler for HTTP get requests.
func requestHandler(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, getEventsPage())
}

// Adds an event to the global events storage.
func AddEvent(event data.Cluster) {
	fmt.Println("Appended a new event")
	for i, c := range events {
		if c.Id == event.Id {
			events[i] = event
			return
		}
	}
	events = append(events, event)
}

/* 
 ******************************************************************************
 *                                  Goroutines
 ******************************************************************************
*/


// Web server goroutine.
func WebHandler() {
	fmt.Println("• WebHandler :: Standing By!")

	// Initialize event slice.
	events = make([]data.Cluster, 0)

	// Start web request handler.
	http.HandleFunc("/", requestHandler)
	log.Fatal(http.ListenAndServe(":8080", nil))
}