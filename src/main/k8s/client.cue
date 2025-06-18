package main

myClientJob: #Job & {
    _name:  "count-letters-client-job"
    _image: "localhost:5001/count-letters-client:v4"
    _container: {
        args: [
            "count-letters-service", 
            "5001",               
            "Hello from k8s",
        ]
    }
}

listObject: {
    apiVersion: "v1"
    kind:       "List"
    items: [ myClientJob ]
}