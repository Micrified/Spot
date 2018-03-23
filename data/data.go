package data

import (
	"time"
	"errors"
	"math"
	"queue"
	"random"
)

const (
	AMP_THRESHOLD	= 10
)

// An atomic sensor data packet.
type Gram struct {
	Id			int
	When 		time.Time
	Location	[]float64 
	Signal 		[]int
}

// Sorting method for the Gram type.
func (g Gram) Compare(x Gram) bool {
	return g.When.Before(x.When)
}

// Returns the coordinates of a Gram.
func (g Gram) Origin() (float64, float64) {
	return g.Location[0], g.Location[1]
}

// Generates a Gram.
func Generate() Gram {
	i := 8
	t := time.Now()
	l := []float64{1.5, 2.5}
	s := []int{0,0,1,2,3,8,10,8,3,2,1,0,0}
	return Gram{i, t, l, s}
}

// Generates a random Gram within a certain zone.
func Random(a Area) Gram {
	i := random.RandomId(46)
	t := random.RandomTime(20)
	x, y := random.RandomLocation(a.X, a.Y, a.Width, a.Height)
	s := random.RandomSignal(10)
	return Gram{i, t, []float64{x,y}, s}
}

// Returns true if a gram is interesting.
func IsInteresting (g Gram) bool {
	for a := range g.Signal {
		if a > AMP_THRESHOLD {
			return true
		}
	}
	return false
}

// A zone descriptor.
type Zone struct {
	Width, Height float64
}

// An area descriptor.
type Area struct {
	X, Y, Width, Height float64
}

// Returns an (i,j) index-pair for a given coordinate over an area divided into zones described by type Zone.
func ZoneIndex (x, y float64, a Area, z Zone) (int, int, error) {
	if (x < a.X) || (x > a.X + a.Width) {
		return 0, 0, errors.New("x coordinate not in area bounds!")
	}
	if (y < a.Y) || (y > a.Y + a.Height) {
		return 0, 0, errors.New("y coordinate not in area bounds!")
	}
	return (int(x - a.X) / int(z.Width)), int(y - a.Y) / int(z.Height), nil
}

// Returns the amount of integer zones to allocate for a given area and zone description.
func ZoneDimensions (a Area, z Zone) (int, int, error) {
	w := math.Ceil(a.Width / z.Width)
	h := math.Ceil(a.Height / z.Height)
	
	if (w < 1.0) || (h < 1.0) {
		return 0, 0, errors.New("Can't subdivide area by given zone!")
	}

	return int(w), int(h), nil
}

type Event struct {
	Queue *queue.Queue
}



