
# SimuladorCrucero

Esta es la tercera y última parte de la situación problema #1 en la que estás enfrentando la problemática del tráfico vehícular en las ciudades. En esta ocasión, se implementará la segunda parte del simulador de cruces vehículares.

El nuevo contexto consiste en que la simulación se hará no sólo para un crucero, sino que se simulará la operación completa de una avenida con muchos cruceros distribuidos en diferentes ubicaciones. Al realizar esto, incluso equivale a la simulación de todas las avenidas de una ciudad si así se desea.

El simulador procesará el paso de vehículos en cada crucero en forma paralela y de manera eficiente, y presentará resultados que le permitirán al departamento de ingeniería vial de la ciudad hacer la gestión administrativa y estratégica correspondiente para la mejora de la movilidad en la ciudad.




## Prerequisitos

Antes de comenzar, asegúrate de que tu máquina cumpla con los siguientes requisitos:

- **Java Development Kit (JDK).**
- **Clojure CLI Tools.**
- **Editor o IDE Compatible.**
- **Leiningen (opcional, para gestionar dependencias y proyectos).**



## Instalación y ejecución

Una vez asegurado que tiene los prerequisitos instalados en su máquina, los pasos son los siguientes:

1. **Clone el repositorio en su máquina.**

2. **Elija el IDE de su preferencia compatible con el lenguaje.**

3. **Una vez dentro de las carpetas:**

    - Primero asegúrese de que tenga el archivo:
      `ciudad_config.txt`
    - Ejecute el archivo `GenerarCruceros.clj` con el comando:  
      ```bash
      clj -M GenerarCruceros.clj
      ```
    - Copie y pegue los archivos generados en la carpeta `SemaforoClojure - copia`.
    - Ejecute el archivo `SemaforoClojure.clj` con el comando:  
      ```bash
      clj -M SemaforoClojure.clj
      ```
