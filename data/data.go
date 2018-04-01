package data

import (
	"time"
	"math"
	"fmt"
	"encoding/json"
)

const (
	AMP_THRESHOLD	= 10.0
)

/* 
 ******************************************************************************
 *                                  Gram
 ******************************************************************************
*/

// An atomic sensor data packet.
type Gram struct {
	Id			int
	When 		int64
	Location	[]float64 
	Signal 		[]float64
}

// Sorting method for the Gram type.
func (g Gram) Compare(x Gram) bool {
	return g.When < x.When
}

// Returns true if given Gram is within certain radius of another.
func (g Gram) Nearby(x Gram, r float64) bool {
	dx := g.Location[0] - x.Location[0]
	dy := g.Location[1] - x.Location[1]
	fmt.Printf("• Sensor %d picked up a signal within %f meters of sensor %d!\n", g.Id, math.Sqrt(dx * dx + dy * dy), x.Id)
	return (r * r >= dx * dx + dy * dy)
}

// Returns true if the given Gram has a signal with amplitude of interest.
func IsInteresting (g Gram) bool {
	for _, v := range g.Signal {
		if v > AMP_THRESHOLD {
			return true
		}
	}
	return false
}

/* 
 ******************************************************************************
 *                                  Cluster
 ******************************************************************************
*/

// A Gram clustering type.
type Cluster struct {
	Id int64
	Updated int64
	Members []Gram
}

// Serializes a Cluster into a JSON byte buffer.
func (c Cluster) Bytes() ([]byte, error) {
	json, err := json.Marshal(c)
	if err != nil {
		return nil, err
	}
	return []byte(json), nil
}

// Inserts a Gram into a Cluster. If an entry exists, it is just updated.
func (c *Cluster) Insert(g Gram) {
	c.Updated = int64(math.Max(float64(g.When), float64(c.Updated)))

	for i, m := range c.Members {
		if (g.Id == m.Id) {
			c.Members[i] = g
			return
		}
	}
	c.Members = append(c.Members, g)
}

// Purges all expired Grams from the given Cluster.
func (c *Cluster) Update(threshold int) {
	cutoff := (time.Now().Unix() * 1000 - int64(threshold))
	var survivors []Gram
	for _, g := range c.Members {
		if g.When > cutoff {
			survivors = append(survivors, g)
		}
	}
	c.Members = survivors
}

// Determines if the Cluster has expired (last added Gram expired).
func (c Cluster) Expired(threshold int) bool {
	cutoff := (time.Now().Unix() * 1000 - int64(threshold))
	return cutoff > c.Updated
}

// Returns true if the given Gram belongs in the Cluster.
func (c Cluster) Suits(g Gram, r float64) bool {
	for _, other := range c.Members {
		if g.Nearby(other, r) {
			return true
		}
	}
	return false
}


