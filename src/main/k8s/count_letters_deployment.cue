package main

myServerDeployment: #Deployment & {
	_name:  "count-letters-server"
	_image: "localhost:5001/count-letters-server:latest" 
    _port:  5001
	_container: {
		ports: [{containerPort: _port}]
		args: ["\(_port)"]
	}
}

myServerService: #Service & {
	_name:        "count-letters-service"
	_port:        myServerDeployment._port
	_selectorApp: "count-letters-server"
}

myClientJob: #Job & {
	_name:  #AppName + "-client-job"
	_image: "localhost:5001/count-letters-client:latest"
	_container: {
		args: [
			myServerService.metadata.name,
			"\(myServerService.spec.ports[0].port)",
			"Hello from k8s",
		]
	}
}

objects: [
	myServerDeployment,
	myServerService,
	myClientJob,
]

listObject: {
	apiVersion: "v1"
	kind:       "List"
	items:      objects
}