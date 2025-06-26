# Obligatorio_SO
Notas del Fix

Recepcionista.java
    -Cambie el nombre de agregarConsultasDelMinuto() a actualizarPrioridadConsultasActivas() ya que solo actualiza
    -El metodo para actualizar solo actualizaba la prioridad de consultas que no habian llegado, cambie el signo para arreglarlo. Solo se debe actualizar la prioridad de las consultas que ya llegaron y estan esperando (tiempodeLlegada > minutoactual)
    -el primer if (linea 40) de atenderConsultasDisponibles() reservaba los recursos a las consultas no llegadas antes que a las que llegaron, se cambio el signo.
    

Consulta.java
    Cambie a que cuando una emergencia sobrepasa el tiempo de espera limite. Esto deberia arreglar las consultas perdidas
