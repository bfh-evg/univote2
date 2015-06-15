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
* Entry for the action manager providing the init action and the dependency graph of the your actions
* Entry for the outcome manager defining the input forms for user input you defined
