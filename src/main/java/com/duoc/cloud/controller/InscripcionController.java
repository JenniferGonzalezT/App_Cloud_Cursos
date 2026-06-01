package com.duoc.cloud.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duoc.cloud.dto.InscripcionRequestDTO;
import com.duoc.cloud.dto.InscripcionResumenDTO;
import com.duoc.cloud.service.InscripcionService;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    // POST /api/inscripciones - Crea la inscripción, genera el resumen y lo sube al bucket S3
    @PostMapping
    public ResponseEntity<String> crearYSubirResumenAS3(@Valid @RequestBody InscripcionRequestDTO request) {
        InscripcionResumenDTO resumen = inscripcionService.inscribir(request);
        inscripcionService.guardarResumenEnS3(resumen);

        return ResponseEntity.ok("Inscripción realizada con éxito. Resumen N° " 
                + resumen.getInscripcionId() + " subido al bucket en su carpeta correspondiente.");
    }

    // GET /api/inscripciones/{id} - Permite descargar el archivo físico desde S3
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> descargarResumenDeS3(@PathVariable Long id) {
        byte[] archivoBytes = inscripcionService.obtenerResumenDeS3(id);
        String fileName = "resumen_" + id + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(archivoBytes);
    }

    // PUT /api/inscripciones/{id} - Permite modificar el contenido de un resumen en el bucket
    @PutMapping("/{id}")
    public ResponseEntity<String> modificarInscripcionYResumenEnS3(@PathVariable Long id, @Valid @RequestBody InscripcionRequestDTO request) {
        InscripcionResumenDTO resumenActualizado = inscripcionService.actualizarInscripcionEnBD(id, request);

        inscripcionService.modificarResumenEnS3(id, resumenActualizado);

        return ResponseEntity.ok("Inscripción N° " + id + " actualizada en Base de Datos y archivo en S3 sobrescrito con éxito.");
    }

    // DELETE /api/inscripciones/{id} - Elimina de forma permanente el objeto dentro de la carpeta en el bucket
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarResumenDeS3(@PathVariable Long id) {
        inscripcionService.eliminarResumenDeS3(id);
        
        return ResponseEntity.ok("Archivo resumen N° " + id + " eliminado exitosamente de S3.");
    }
}