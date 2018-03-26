package data

import (
	"time"
	"math"
	"fmt"
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

// Returns true if a given gram is within a radius of another gram.
func (g Gram) Nearby(x Gram, r float64) bool {
	dx := g.Location[0] - x.Location[0]
	dy := g.Location[1] - x.Location[1]
	fmt.Printf("• Sensor %d picked up a signal within %f kilometers of sensor %d!\n", g.Id, math.Sqrt(dx * dx + dy * dy), x.Id)
	return (r * r >= dx * dx + dy * dy)
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

type Cluster struct {
	Updated time.Time
	Members []Gram
}

func (c *Cluster) Insert(g Gram) {
	c.Members = append(c.Members, g)
	if g.When.After(c.Updated) {
		c.Updated = g.When
	}
}

func (c *Cluster) Update(threshold int) {
	cutoff := time.Now().Add(-time.Second * time.Duration(threshold))
	var survivors []Gram
	for _, g := range c.Members {
		if g.When.After(cutoff) {
			survivors = append(survivors, g)
		}
	}
	c.Members = survivors
}

func (c Cluster) Expired(threshold int) bool {
	cutoff := time.Now().Add(-time.Second * time.Duration(threshold))
	return cutoff.After(c.Updated)
}

func (c Cluster) Suits(g Gram, r float64) bool {
	//fmt.Println("• Members has length: ", len(c.Members))
	for _, other := range c.Members {
		//fmt.Println("• Comparing: ", g, " to ", other)
		if g.Nearby(other, r) {
			return true
		}
	}
	return false
}
