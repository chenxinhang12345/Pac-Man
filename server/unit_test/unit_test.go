package unit_test

import (
	"Pac-Man/server/maze"
	"Pac-Man/server/network"
	"bufio"
	"fmt"
	"net"
	"strings"
	"testing"

	"github.com/sirupsen/logrus"
)

func TestServer(t *testing.T) {
	go network.TCPListen()
	for i := 0; i < 5; i++ {
		go t.Run("Test Server Connection", func(t *testing.T) {
			conn, err := net.Dial("tcp", ":4321")
			if err != nil {
				logrus.Error(err)
			}
			defer conn.Close()
			reader := bufio.NewReader(conn)
			for {
				str, err := reader.ReadString('\n')
				if err != nil {
					// if err == io.EOF {
					// conn.Close()
					// return
					// }
					logrus.Error(err)
				}
				tokens := strings.Split(str, ";")
				if tokens[0] == "USERINFO" {
					fmt.Printf("USERINFO: %s", tokens[1])
				} else if tokens[0] == "NEWUSER" {
					fmt.Printf("NEWUSER: %s", tokens[1])
				}
			}

			// if str != "USERINFO\n" {
			// 	logrus.Errorf("Wrong response: %s", str)
			// }
			// size := make([]byte, 8)
			// n, err := reader.Read(size)
			// if err != nil || n != 8 {
			// 	logrus.Errorf("Error when read data size: %s  readed bytes: %d", err, n)
			// }
			// length := int(binary.BigEndian.Uint64(size))
			// fmt.Println(length)
			// data := make([]byte, length)
			// n, err = reader.Read(data)
			// if err != nil || n != length {
			// 	logrus.Errorf("Error when read data: %s  read length: %d", err, n)
			// }
			// fmt.Println(string(data))

		})
	}
	select {}
}

func bytes2int(bytes []byte) int {
	if len(bytes) != 8 {
		panic("Not the size of bytes!")
	}
	var value int
	for i := 0; i < 8; i-- {
		value |= int(bytes[i]) << uint32((7-i)*8)
	}
	return value
}

func TestMaze(t *testing.T) {
	for i := 0; i < 3; i++ {
		t.Run("Test maze construction", func(t *testing.T) {
			m := maze.NewMaze()
			// m.SetUp()
			fmt.Println(m.Cells)
		})
	}
}
