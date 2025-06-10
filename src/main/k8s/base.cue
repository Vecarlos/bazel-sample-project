//reference https://github.com/world-federation-of-advertisers/cross-media-measurement/blob/main/src/main/k8s/base.cue
package main

#AppName: "count-letters"

#ObjectMeta: {
	name: string
	labels?: [string]: string
}

#Container: {
	name:   string
	image:  string
	ports?: [...{containerPort: int}]
	args?:             [...string]
	imagePullPolicy: "IfNotPresent" | *"Always"
}

#Deployment: {
	_name:     string
	_image:    string
	_replicas: int | *1
	_container: #Container
	
	apiVersion: "apps/v1"
	kind:       "Deployment"
	metadata: #ObjectMeta & {
		name: _name
		labels: {
			app: _name
		}
	}
	spec: {
		replicas: _replicas
		selector: matchLabels: {
			app: _name
		}
		template: {
			metadata: {
				labels: {
					app: _name
				}
			}
			spec: containers: [
				_container & {
					name:  _name
					image: _image
				},
			]
		}
	}
}

#Service: {
	_name:        string
	_selectorApp: string
	_port:        int
	
	apiVersion: "v1"
	kind:       "Service"
	metadata: #ObjectMeta & {
		name: _name
		labels: {
			app: _name
		}
	}
	spec: {
		selector: {
			app: _selectorApp
		}
		ports: [{
			port:       _port
			targetPort: _port
			name:       "grpc"
		}]
		type: "ClusterIP"
	}
}

#Job: {
	_name:      string
	_image:     string
	_container: #Container
	
	apiVersion: "batch/v1"
	kind:       "Job"
	metadata: #ObjectMeta & {
		name: _name
		labels: {
			app: _name
		}
	}
	spec: {
		template: {
			spec: {
				containers: [
					_container & {
						name:  _name
						image: _image
					},
				]
				restartPolicy: "Never"
			}
		}
		backoffLimit: 1
	}
}