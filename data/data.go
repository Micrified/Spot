package data

import (
	"time"
	"errors"
	"math"
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

// Generates a Gram.
func Generate() Gram {
	i := 8;
	t := time.Now().AddDate(0, -1, 0)
	l := []float64{64.0, 22.0}
	s := []int{9,0,1,1,4,5,6,3,2,1,1,0,0}
	return Gram{i, t, l, s}
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

