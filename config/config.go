package config

import (
	"fmt"
	"errors"
	"os"
	"data"
	"bytes"
	"encoding/json"
)

type Config struct {
	Time_Threshold int
	Sensor_Threshold int
	Area_X, Area_Y, Area_Width, Area_Height float64
	Zone_Width, Zone_Height float64
}

// Returns the Area of a Configuration object.
func (c Config) Area() data.Area {
	return data.Area{c.Area_X, c.Area_Y, c.Area_Width, c.Area_Height}
}

// Returns the Zone of a Configuration object.
func (c Config) Zone() data.Zone {
	return data.Zone{c.Zone_Width, c.Zone_Height}
}

// Opens a file and returns it as a byte buffer.
func OpenFileStream(fileName string) ([]byte, error) {
	var file *os.File
	var err error
	if file, err = os.Open(fileName); err != nil {
		err = fmt.Errorf("Can't open config file \"%s\"\n", fileName)
		return nil, err
	}
	b := new(bytes.Buffer)
	b.ReadFrom(file)
	return b.Bytes(), nil
}

// Reads a config file.
func ReadConfig(stream []byte) (Config, error) {
	var c Config
	err := json.Unmarshal(stream[:len(stream)], &c)
	if err != nil {
		return Config{}, errors.New("Couldn't unmarshal the data stream!")
	}
	return c, nil
}
