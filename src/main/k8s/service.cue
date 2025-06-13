package main

myServerService: #Service & {
    _name:        "count-letters-service"
    _port:        5001 
    _selectorApp: "count-letters-server"
}

listObject: {
    apiVersion: "v1"
    kind:       "List"
    items: [ myServerService ]
}