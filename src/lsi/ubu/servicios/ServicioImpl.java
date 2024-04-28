package lsi.ubu.servicios;

import java.sql.Connection;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Time;

import java.util.Date;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import lsi.ubu.excepciones.CompraBilleteTrenException;

import lsi.ubu.util.PoolDeConexiones;

public class ServicioImpl implements Servicio {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServicioImpl.class);

	@Override

	public void anularBillete(Time hora, java.util.Date fecha, String origen, String destino, int nroPlazas, int ticket)

			throws SQLException {

		PoolDeConexiones pool = PoolDeConexiones.getInstance();

		/* Conversiones de fechas y horas */

		java.sql.Date fechaSqlDate = new java.sql.Date(fecha.getTime());

		java.sql.Timestamp horaTimestamp = new java.sql.Timestamp(hora.getTime());

		Connection con = null;

		PreparedStatement st = null;

		ResultSet rs = null;

		// A completar por el alumno

		try {

			con = pool.getConnection();

			// 1º Probamos que exista el ticket

			st = con.prepareStatement("SELECT idTicket "

					+ "FROM tickets "

					+ "WHERE idTicket = ?");

			st.setInt(1, ticket);

			rs = st.executeQuery();

			// si no encuentra un ticket lanza la excepcion

			if (!rs.next())
				throw new CompraBilleteTrenException(CompraBilleteTrenException.NO_EXISTE_VIAJE);

			// 2º Comprobamos que el viaje no se efectuo

			int idViaje = rs.getInt("idViaje");

			st = con.prepareStatement("SELECT realizado "

					+ "FROM viajes "

					+ "WHERE idViaje = ?");

			st.setInt(1, idViaje);

			rs = st.executeQuery();

			if (!rs.next() || rs.getInt("realizado") == 1) {

				LOGGER.error("No se puede anular un viaje ya pasado.");

			}

			// 3º Tomamos las plazas que se liberan del ticket anulado y actualizamos el
			// viaje

			int plazasLiberadas = rs.getInt("cantidad");

			st = con.prepareStatement("UPDATE viajes "

					+ "SET nPlazasLibres = nPlazasLibres + ? "

					+ "WHERE idViaje = ?");

			st.setInt(1, plazasLiberadas);

			st.setInt(2, idViaje);

			st.executeUpdate();

			// comiteamos la transaccion

			con.commit();

		} catch (SQLException e) {

			LOGGER.error("Error al anular el billete: " + e.getMessage());

			con.rollback();
			; // Realizamos el rollback de la transacción

		} finally {

			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();

				if (con != null)
					con.close();

				LOGGER.info("Conexion cerrada.");

				LOGGER.info("La conexion es valida: " + con.isValid(0));

			} catch (SQLException e) {

				LOGGER.error("Error al cierre de la conexion.");

				LOGGER.error(e.getMessage());

			}

		}

	}

	@Override

	public void comprarBillete(Time hora, Date fecha, String origen, String destino, int nroPlazas)

			throws SQLException {

		PoolDeConexiones pool = PoolDeConexiones.getInstance();

		/* Conversiones de fechas y horas */

		java.sql.Date fechaSqlDate = new java.sql.Date(fecha.getTime());

		java.sql.Timestamp horaTimestamp = new java.sql.Timestamp(hora.getTime());

		Connection con = null;

		PreparedStatement st = null;

		ResultSet rs = null;

		// A completar por el alumno : desde aquí

		try {

			con = pool.getConnection();

			// Verificar si el viaje existe

			st = con.prepareStatement(

<<<<<<< HEAD
					"SELECT v.idViaje " +

							"FROM viajes v " +

							"INNER JOIN recorridos r ON v.idRecorrido = r.idRecorrido " +

							"WHERE r.estacionOrigen = ? AND r.estacionDestino = ? AND v.fecha = ?");

			st.setString(1, origen);

			st.setString(2, destino);

			st.setDate(3, fechaSqlDate);

			rs = st.executeQuery();

			if (!rs.next()) {

				// No se encontró el viaje

				throw new SQLException("No existe el viaje especificado.", null,
						CompraBilleteTrenException.NO_EXISTE_VIAJE);

			}

			int idViaje = rs.getInt("idViaje");

			// Verificar si hay plazas disponibles

			st = con.prepareStatement(

					"SELECT (m.nPlazas - COALESCE(SUM(t.cantidad), 0)) AS plazas_disponibles " +

							"FROM modelos m " +

							"INNER JOIN trenes tr ON m.idModelo = tr.modelo " +

							"LEFT JOIN viajes v ON tr.idTren = v.idTren " +

							"LEFT JOIN tickets t ON v.idViaje = t.idViaje " +

							"WHERE v.idViaje = ? " +

							"GROUP BY m.nPlazas");

			st.setInt(1, idViaje);

			rs = st.executeQuery();

			if (!rs.next() || rs.getInt("plazas_disponibles") < nroPlazas) {

				// No hay plazas disponibles

				throw new SQLException("No hay plazas disponibles para el viaje especificado.", null,
						CompraBilleteTrenException.NO_PLAZAS);

			}

			// Obtenemos el valor del precio para calcular

			st = con.prepareStatement(

					"SELECT r.precio " +

							"FROM recorridos r " +

							"INNER JOIN viajes v ON r.idRecorrido = v.idRecorrido " +

							"WHERE v.idViaje = ?");

			st.setInt(1, idViaje);

			rs = st.executeQuery();

			int precioRecorrido;

			if (rs.next()) {

				precioRecorrido = rs.getInt("precio");

			}

			// Calculamos el precio total del viaje

			precioRecorrido = rs.getInt("precio"); // Obtener el precio del recorrido

			int precioTotal = nroPlazas * precioRecorrido; // Calcular el precio total

			// Insertamos la fila en la tabla de tickets

			st = con.prepareStatement(

					"INSERT INTO tickets (idTicket, idViaje, fechaCompra, cantidad, precio) " +

							"VALUES (seq_tickets.nextval, ?, ?, ?, ?)");

			st.setInt(1, idViaje);

			st.setDate(2, fechaSqlDate);

			st.setInt(3, nroPlazas);

			st.setInt(4, precioTotal); // Multiplicar el precio por el número de plazas

			// st.setString(5, origen);

			// st.setString(6, destino);

			st.executeUpdate();

			LOGGER.info("Compra de billetes realizada exitosamente.");

		} catch (SQLException e) {

			LOGGER.error("Error al realizar la compra de billetes: " + e.getMessage());

			throw e;

		} finally {

			// Cerrar recursos

			if (rs != null) {

				rs.close();

			}

			if (st != null) {

				st.close();

			}

			if (con != null) {

				con.close();

			}

		}
=======
		        // Insertamos la fila en la tabla de tickets
		        st = con.prepareStatement(
		        	    "INSERT INTO tickets (idTicket, idViaje, fechaCompra, cantidad, precio) " +
		        	    "VALUES (seq_tickets.nextval, ?, ?, ?, ?)");
		        	st.setInt(1, idViaje);
		        	st.setDate(2, fechaSqlDate);
		        	st.setInt(3, nroPlazas);
		        	st.setInt(4, precioTotal); // Multiplicar el precio por el número de plazas
		        	//st.setString(5, origen);
		        	//st.setString(6, destino);
		        	st.executeUpdate();
			 
			// Actualizar el número de plazas libres en el viaje tras la compra del billete
		       	st = con.prepareStatement(
		       	    "UPDATE viajes SET nPlazasLibres = nPlazasLibres - ? WHERE idViaje = ?");
		       	st.setInt(1, nroPlazas); // Número de plazas compradas
		       	st.setInt(2, idViaje);
		       	st.executeUpdate();
>>>>>>> c0272adc1e2db24b935fcc734054a0b4d2b141f7

	}

}