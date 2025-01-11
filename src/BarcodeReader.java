package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BarcodeReader {

	private final int MINIMUM_LENGHT = 12;
	private final int MAXIMUM_LENGHT = 13;

	@GetMapping("/barcodereader/{barcode}")
	public ResponseEntity<Object> getDateFromCode(@PathVariable String barcode) {
		/*
		 * Verifica que el codigo de barras tiene una longitud de entre 12 y 13 caracteres
		 */
		if (barcode.length() < MINIMUM_LENGHT || barcode.length() > MAXIMUM_LENGHT) {
			return new ResponseEntity<>(
					new Message("Longitud incorrecta: el código de barras debe tener una longitud de entre 12 y 13 caracteres. El código de barras ingresado tiene una longitud de " + barcode.length() + " caracteres."),
					HttpStatus.BAD_REQUEST);
		}

		// Convierte las minusculas a mayusculas
		String barcodeUpperCase = barcode.toUpperCase();

		/*
		 * Verifica que el codigo de barras ingresado tiene el formato esperado:
		 * - 4 digitos para el lote
		 * - 1 caracter alfabetico correspondiente a la letra inicial
		 * del nombre de un mes
		 * - 1 digito para el año
		 * - 2 o 3 digitos para el bulto
		 * - la palabra fija ACME al final
		 */
		if (!barcodeUpperCase.matches("^[0-9]{4}[EFMJASOND]\\d{1}[0-9]{2,3}ACME$")) {
			return new ResponseEntity<>(new Message(
					"El código de barras no tiene el formato esperado: debe comenzar con 4 dígitos (lote), seguidos de 1 carácter alfabético correspondiente a la letra inicial del nombre de un mes, seguido de 1 dígito (año), seguido de 2 o 3 dígitos (bulto), seguidos de la palabra fija ACME"),
					HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(extractBarcodeData(barcodeUpperCase), HttpStatus.OK);
	}

	/**
	 * 
	 * @param barcode
	 * @return referencia a un objeto de tipo BarcodeData que contiene el codigo de
	 *         barras, el lote extendido y el bulto
	 */
	private BarcodeData extractBarcodeData(String barcode) {
		BarcodeData barcodeData = new BarcodeData(barcode, extractExtendedBatch(barcode), extractBundle(barcode));
		return barcodeData;
	}

	/**
	 * 
	 * @param barcode
	 * @return referencia a un objeto de tipo String que contiene el lote
	 * 		   extendido de un codigo de barras
	 */
	private String extractExtendedBatch(String barcode) {
		return barcode.substring(0, 6);
	}

	/**
	 * 
	 * @param barcode
	 * @return referencia a un objeto de tipo String que contiene el valor
	 * 		   correspondiente al campo del bulto de un codigo de barras
	 */
	private String extractBundle(String barcode) {
		/*
		 * Si el codigo de barras tiene la longitud minima (12), el campo del bulto se
		 * encuentra entre el caracter de la posicion 6 y el caracter de la posicion 8 -
		 * 1.
		 * 
		 * Ejemplo: 0 0 0 1 E 4 0 1 A C M  E
		 * 			0 1 2 3 4 5 6 7 8 9 10 11
		 */
		if (barcode.length() == MINIMUM_LENGHT) {
			return barcode.substring(6, 8);
		}

		/*
		 * Si el codigo de barras tiene la longitud maxima (13), el campo del bulto se
		 * encuentra entre el caracter de la posicion 6 y el caracter de la posicion 9 -
		 * 1.
		 * 
		 * Ejemplo: 2 0 0 0 D 4 0 0 1 A C  M  E
		 * 			0 1 2 3 4 5 6 7 8 9 10 11 12
		 */
		return barcode.substring(6, 9);
	}

	/*
	 * Clase privada utilizada para devolver el codigo de barras, el lote extendido y el bulto
	 */
	private class BarcodeData {

		private String barcode;
		private String extendedBatch;
		private String bundle;

		public BarcodeData(String barcode, String extendedBatch, String bundle) {
			super();
			this.barcode = barcode;
			this.extendedBatch = extendedBatch;
			this.bundle = bundle;
		}

		@SuppressWarnings("unused")
		public String getBarcode() {
			return barcode;
		}

		@SuppressWarnings("unused")
		public String getExtendedBatch() {
			return extendedBatch;
		}

		@SuppressWarnings("unused")
		public String getBundle() {
			return bundle;
		}

	}

	/*
	 * Clase utilizada para devolver mensajes de error
	 */
	private class Message {

		private String message;

		public Message(String message) {
			super();
			this.message = message;
		}

		@SuppressWarnings("unused")
		public String getMessage() {
			return message;
		}

	}

}
