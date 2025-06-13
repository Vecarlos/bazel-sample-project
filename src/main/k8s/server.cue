package main

myServerDeployment: #Deployment & {
    _name:  "count-letters-server"
    _image: "count-letters-server:latest" 
    _port:  5001
    _container: {
        ports: [{containerPort: _port}]
        args: ["\(_port)"]
    }
}

listObject: {
    apiVersion: "v1"
    kind:       "List"
    items: [ myServerDeployment ]
}