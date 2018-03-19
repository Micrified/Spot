package heap

import (
	"fmt"
	"container/heap"
	"data"
)

type Heap []data.Gram

// Init method for the heap.
func (h *Heap) Init () {
	heap.Init(h)
}

// Push method for the heap.
func (h *Heap) Push(x interface{}) {
	*h = append(*h, x.(data.Gram))
}

// Pop method for the heap.
func (h *Heap) Pop() interface{} {
	old := *h
	n := len(old)
	x := old[n - 1]
	*h = old[0:n - 1]
	return x
}

// Sorting method for the heap.
func (h Heap) Less(i, j int) bool {
	return h[i].Compare(h[j])
}

// Swapping method for the heap.
func (h Heap) Swap(i, j int) {
	h[i], h[j] = h[j], h[i]
}

// Printing method for the heap.
func (h Heap) Print () {
	fmt.Println("-------------------- HEAP --------------------")
	for i := 0; i < len(h); i++ {
		fmt.Println(i, ": ", h[i])
	}
	fmt.Println("-------------------- ---- --------------------")
}

// Returns length of the heap.
func (h Heap) Len() int {
	return len(h)
}

// Returns capacity of the heap.
func (h Heap) Cap() int {
	return cap(h)
}