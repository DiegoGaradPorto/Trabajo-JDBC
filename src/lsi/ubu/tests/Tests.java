package lsi.ubu.tests;



import java.sql.Connection;



import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Time;

import java.text.ParseException;

import java.text.SimpleDateFormat;



import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



import lsi.ubu.excepciones.CompraBilleteTrenException;

import lsi.ubu.servicios.Servicio;

import lsi.ubu.servicios.ServicioImpl;

import lsi.ubu.util.PoolDeConexiones;



public class Tests {



	/** Logger. */

	private static final Logger LOGGER = LoggerFactory.getLogger(Tests.class);



	public static final String ORIGEN = "Burgos";

	public static final String DESTINO = "Madrid";



	public void ejecutarTestsAnularBilletes() {
		Servicio servicio = new ServicioImpl();
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con = null;
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			// Prueba caso anular billete existente
			LOGGER.info("Prueba anular billete existente");
			Time hora = Time.valueOf("12:00:00");
			java.util.Date fecha = toDate("27/04/2024");
			int nroPlazas = 2;
			int ticketExistente = 1;
			servicio.anularBillete(hora, fecha, ORIGEN, DESTINO, nroPlazas, ticketExistente);
			LOGGER.info("Anulación de billete existente OK");

			// Prueba caso anular billete inexistente
			LOGGER.info("Prueba anular billete inexistente");
			int ticketInexistente = 999; // Suponiendo que el billete con ID 999 no existe
			try {
				servicio.anularBillete(hora, fecha, ORIGEN, DESTINO, nroPlazas, ticketInexistente);
				LOGGER.error("Anulación de billete inexistente fallida: no se lanzó una excepción de billete no encontrado");
			} catch (SQLException e) {
				LOGGER.info("Se da cuenta de que el billete no existe OK: " + e.getMessage());
			}

			// Prueba caso anular billete para un viaje ya realizado
			LOGGER.info("Prueba anular billete para un viaje ya realizado");
			int ticketViajeRealizado = 2; // Suponiendo que el billete con ID 2 está asociado a un viaje ya realizado
			try {
				servicio.anularBillete(hora, fecha, ORIGEN, DESTINO, nroPlazas, ticketViajeRealizado);
				LOGGER.error("Anulación de billete para un viaje ya realizado fallida: no se lanzó una excepción de viaje ya realizado");
			} catch (SQLException e) {
				LOGGER.info("Se da cuenta de que el viaje ya se ha realizado OK: " + e.getMessage());
			}
		} catch (SQLException e) {
			LOGGER.error("Error al anular el billete: " + e.getMessage());
		}
	}



	public void ejecutarTestsCompraBilletes() {



		Servicio servicio = new ServicioImpl();



		PoolDeConexiones pool = PoolDeConexiones.getInstance();



		Connection con = null;

		PreparedStatement st = null;

		ResultSet rs = null;



		// Prueba caso no existe el viaje



		  try {

			  

			java.util.Date fecha = toDate("15/04/2010");

			Time hora = Time.valueOf("12:00:00");

			int nroPlazas = 3;

			

			// se hace una compra, pasándole los valores de las variables

			servicio.comprarBillete(hora, fecha, ORIGEN, DESTINO, nroPlazas);



			LOGGER.info("NO se da cuenta de que no existe el viaje MAL");

		} catch (SQLException e) {

			if (e.getErrorCode() == CompraBilleteTrenException.NO_EXISTE_VIAJE) {

				LOGGER.info("Se da cuenta de que no existe el viaje OK");

			}

		}



		// Prueba caso si existe pero no hay plazas



		try {

			java.util.Date fecha = toDate("20/04/2022");

			Time hora = Time.valueOf("8:30:00");

			int nroPlazas = 50;



			servicio.comprarBillete(hora, fecha, ORIGEN, DESTINO, nroPlazas);



			LOGGER.info("NO se da cuenta de que no hay plazas MAL");

		} catch (SQLException e) {

			if (e.getErrorCode() == CompraBilleteTrenException.NO_PLAZAS) {

				LOGGER.info("Se da cuenta de que no hay plazas OK");

			}

		}



		// Prueba caso si existe y si hay plazas

		  try {

			java.util.Date fecha = toDate("20/04/2022");

			Time hora = Time.valueOf("8:30:00");

			int nroPlazas = 5;



			servicio.comprarBillete(hora, fecha, ORIGEN, DESTINO, nroPlazas);



			con = pool.getConnection();

			st = con.prepareStatement(

					" SELECT IDVIAJE||IDTREN||IDRECORRIDO||FECHA||NPLAZASLIBRES||REALIZADO||IDCONDUCTOR||IDTICKET||CANTIDAD||PRECIO "

							+ " FROM VIAJES natural join tickets "

							+ " where idticket=3 and trunc(fechacompra) = trunc(current_date) ");

			rs = st.executeQuery();



			String resultadoReal = "";

			while (rs.next()) {

				resultadoReal += rs.getString(1);

			}



			String resultadoEsperado = "11120/04/2225113550";

			// LOGGER.info("R"+resultadoReal);

			// LOGGER.info("E"+resultadoEsperado);

			if (resultadoReal.equals(resultadoEsperado)) {

				LOGGER.info("Compra ticket OK");

			} else {

				LOGGER.info("Compra ticket MAL");

			}



		} catch (SQLException e) {

			LOGGER.info("Error inesperado MAL");

		}

	}



	private java.util.Date toDate(String miString) { // convierte una cadena en fecha

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); // Las M en mayusculas porque sino interpreta

																		// minutos!!

			java.util.Date fecha = sdf.parse(miString);

			return fecha;

		} catch (ParseException e) {

			LOGGER.error(e.getMessage());

			return null;

		}

	}

}

