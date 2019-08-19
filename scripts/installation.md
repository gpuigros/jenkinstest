# Instalación

## Configurar script para autoaprobación de scripts

En el apartado de configuración de Scriptler de jenkins: 

![Apartado scriptler][scriptler1]

Añadir un nuevo script con los siguientes datos:

- id: `autoapproved-groovy-scripts.groovy`
- name: `Auto approval pending groovy scripts`
- comment: `Put "// @autoapproved"  at the top of the groovy script`
- premission: `true`
- restriction: `true`
- Define script parameters: `true`
- name: `approvalPrefix`
- default: `// @Autoapproved`
- script:
 
```
import jenkins.model.Jenkins

def pendingApprovalScripts = Jenkins.instance.getExtensionList('org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval')
println "pendingApprovalScripts: $pendingApprovalScripts"

pendingApprovalScripts.each { scriptApproval ->
    def hashesToApprove = scriptApproval.pendingScripts.findAll{ it.script.startsWith(approvalPrefix) }.collect{ it.getHash() }
    hashesToApprove.each { 
        println "Approve hasth: $it"
        scriptApproval.approveScript(it)
    }
}
```

El nuevo script debería quedar de la siguiente forma: 

![Datos del script][scriptler2]


## Creación del seed job

Crear un free style job en jenkins con nombre `create_seed_jobs`

Marca la casilla `"Esta ejecución debe parametrizarse"` y añadir un parámetro de cadena con nombre `DESTINATION_PATH` y el valor por defecto que 
interese. En el path que se indique se crearan los jobs que generan la configuración y el pipeline para un repositorio.

Añadir otro parámetro de tipo elección con nombre `MODE` y opciones (una por línea) `NORMAL` y `RELEASES_COMPATIBILITY`

Marcar la casilla `"Restringir dónde se puede ejecutar este proyecto"` y añadirle el label `master`.

Añadir el repositorio git `ssh://git@stash.hotelbeds:7999/envtools/jenkins-pipelines-scripts.git` seleccionando como credenciales `stash`, y como rama 
`*/master`

Además añadir como `Additional Behaviours - Check out to a sub-directory` con valor `scripts`

En el apartado `"Ejecutar"` añadir una tarea `"Process Job DSLs"` configurada de la siguiente forma:

- Marcar "Look on Filesystem"
- DSL Scripts: `scripts/src/main/groovy/CreateSeedJobs.groovy`
- Additional classpath: `scripts/lib/*.jar`

Añadir una tarea de tipo `"Scriptler script"` configurada de la siguiente forma:

- script `Auto approval pending groovy scripts`
- Marcar la casilla `Define script parameters`
- Añadir el parámetro `approvalPrefix`  con valor `//@Autoapproved`

Guardar el job.

Imagenes de como debe quedar el job:

![Configuración del job 01][img01]
![Configuración del job 02][img02]
![Configuración del job 03][img03]
![Configuración del job 04][img04]
![Configuración del job 05][img05]
![Configuración del job 06][img06]


Una vez configurado el job, se puede ejecutar indicando el path donde generar los jobs.
La ejecución creará los dos jobs principales para configurar y crear un pipeline.

- `buildblock-pipeline_CONFIG_GENERATOR`: Crea el pipeline.yml
- `buildblock-pipeline_GENERATOR`: Crea el job regenerator para el pipeline definido en el yml generado por el job anterior.


[scriptler1]: doc-resources/img/install/Scriptler001.png
[scriptler2]: doc-resources/img/install/Scriptler002.png
[img01]: doc-resources/img/install/Image001.png
[img02]: doc-resources/img/install/Image002.png
[img03]: doc-resources/img/install/Image003.png
[img04]: doc-resources/img/install/Image004.png
[img05]: doc-resources/img/install/Image005.png
[img06]: doc-resources/img/install/Image006.png