package random

import (
	"math/rand"
	"time"
)

func RandomInt (n int) int {
	rand.Seed(time.Now().UTC().UnixNano())
	return rand.Intn(n)
}

func RandomFloat (n float64) float64{
	rand.Seed(time.Now().UTC().UnixNano())
	return n * rand.Float64()
}

func RandomId (max int) int {
	return RandomInt(max)
}

func RandomTime (tolerance int) time.Time {
	d := int64(RandomInt(tolerance))
	t := time.Second * time.Duration(d - (d / 2))
	return time.Now().Add(t)
}

func RandomLocation (x, y, w, h float64) (float64, float64) {
	return x + RandomFloat(w), y + RandomFloat(h)
}

func RandomSignal (n int) []int {
	var s []int
	for i := 0; i < n; i++ {
		s = append(s, RandomInt(20))
	}
	return s
}