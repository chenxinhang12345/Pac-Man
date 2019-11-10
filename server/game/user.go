package game

import (
	"bufio"
	"encoding/json"
	"io"
	"math/rand"
	"net"
	"time"

	"github.com/sirupsen/logrus"
)

type User struct {
	ID    int
	X     int
	Y     int
	Color int
	Score int
	Conn  net.Conn
	MQ    chan string
}

func NewUser(conn net.Conn) User {
	id := rand.Intn(1000)
	Users.Mux.Lock()
	for _, ok := Users.Users[id]; ok == true; id = rand.Intn(1000) {
		_, ok = Users.Users[id]
	}
	user := User{
		ID:    id,
		X:     rand.Intn(500),
		Y:     rand.Intn(500),
		Conn:  conn,
		Color: -rand.Intn(16777215),
		MQ:    make(chan string, 1024),
	}
	Users.Users[id] = user
	Users.Mux.Unlock()
	return user
}

func (user User) HandleRead() {
	reader := bufio.NewReader(user.Conn)
	for {
		user.Conn.SetReadDeadline(time.Now().Add(10 * time.Second))
		str, err := reader.ReadString('\n')
		if err != nil {
			if err == io.EOF {
				user.Conn.Close()
				return
			}
			logrus.Errorln("Error when read from TCP", err)
		}
		logrus.Println(str)
	}
}

func (user User) HandleWrite() {
	writer := bufio.NewWriter(user.Conn)
	select {
	case msg := <-user.MQ:
		writer.Write([]byte(msg))
		writer.Flush()
	}
}

func (user User) ToString() string {
	return string(user.ToBytes())
}

func (user User) ToBytes() []byte {
	info := struct {
		ID    int
		Color int
		X     int
		Y     int
		Score int
	}{
		ID:    user.ID,
		Color: user.Color,
		X:     user.X,
		Y:     user.Y,
		Score: user.Score,
	}
	userMarshal, err := json.Marshal(info)
	if err != nil {
		panic(err)
	}
	return userMarshal
}
