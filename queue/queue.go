package queue

import "errors"

type Queue []interface{}

// Append item to queue.
func (q *Queue) Enqueue(x interface{}) {
	*q = append(*q, x)
}

// Return next item in the queue.
func (q *Queue) Dequeue() (interface{}, error) {
	theQueue := *q
	if len(theQueue) <= 0 {
		return nil, errors.New("Can't dequeue() from empty queue!")
	}
	x := theQueue[0]
	*q = theQueue[1:]
	return x, nil
}

// Returns the top item in the queue.
func (q Queue) Peek() (interface{}, error) {
	if len(q) <= 0 {
		return nil, errors.New("Can't peek() from empty queue!")
	}
	return q[0], nil
}

// Returns length of the queue.
func (q Queue) Len() int {
	return len(q)
}

// Returns capacity of the queue.
func (q Queue) Cap() int {
	return cap(q)
}

// Returns true if the queue is empty.
func (q Queue) IsEmpty() bool {
	return len(q) == 0
}