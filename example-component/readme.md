# GUIDE TO CREATE A NEW COMPONENT
Copy this folder to the location of your new component.
## THINGS TO CHANGE 
* Change the name and artifcat-id in the 2 maven poms.
* Replace the actions in the ejb project with ur own actions.
* Rewrite the initAction to fit your actions
* Add the configuration in your own custom JNDI
* Replace the input forms in the war project with your own.
* Change the JNDI_URI resource in the ejb-jar.xml in the war project to link your custom JNDI entry
* Change the faces-config.xml to fit your input forms.
* Configure the peristence.xml in the war project to use the right persistence.

## JNDI_CUSTOM Resources
* Entry for the configuration manager to find the other configurations

| name  | value  |
|---|---|
| outcome-routing  | univote2/outcome-routing  |
| uniboard-helper  | univote2/uniboard-helper  |
| action-graph  | univote2/action-graph  |
* Entry for the action manager providing the init action and the dependency graph of the your actions
 
| name  | value  |
|---|---|
| InitAction  | ParallelAction,OtherAction  |
| initialAction  | InitAction |
* Entry for the outcome manager defining the input forms for user input you defined

| name  | value  |
|---|---|
| InitInput | initActionInput  |
| ParallelValue  | parallelActionInput  |

* Entry for the uniboard helper containing urls and the board certificate

| name  | value  |
|---|---|
| g | 7  |
| q | 11  |
| p | 29  |
| y | 3  |
| endPointUrl | http://localhost/andSoOn  |
| wsdlLocation | http://localhost/andSoOn   |
