package main

myServerDeployment: #Deployment & {
    _name:  "count-letters-server"
    _image: "localhost:5001/count-letters-server:v4" 
    _port:  5001
    _container: {
        ports: [{containerPort: _port}]
        args: ["\(_port)"]
    }
}
