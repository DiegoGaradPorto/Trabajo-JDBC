package lsi.ubu.servicios;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		// A completar por el alumno
				
		//Comprobamos si existe un viaje
		try {
	        con = pool.getConnection();
	     // Comenzamos una transacción
	        con.setAutoCommit(false);
	        
	        // Buscamos el viaje correspondiente
	        String buscarViajeSQL = "SELECT v.idViaje, v.nPlazasLibres, r.precio FROM viajes v " +
	                                "JOIN recorridos r ON v.idRecorrido = r.idRecorrido " +
	                                "WHERE r.estacionOrigen = ? AND r.estacionDestino = ? AND v.fecha = ? AND r.horaSalida = ?";
	        st = con.prepareStatement(buscarViajeSQL);
	        st.setString(1, origen);
	        st.setString(2, destino);
	        st.setDate(3, fechaSqlDate);
	        st.setTimestamp(4, horaTimestamp);
	        rs = st.executeQuery();
	        
	        
	        // Verificar que el viaje existe
	        if (rs.next()) {
	        	LOGGER.info("SI existe el viaje");
	        	
	        	//Guardamos los valores de las plazas y del idViaje
	        	int plazasLibres = rs.getInt("nPlazasLibres");
	        	int idDelViaje = rs.getInt("idViaje");
	        	double precioBillete = rs.getDouble("precio");
	        	
	        	//Verificamos si hay plazas libres en este viaje
	        	if (plazasLibres >= nroPlazas) {
	        		
	        		//Decrementamos el número de plazas libres en la variable
	        		plazasLibres = plazasLibres - nroPlazas;
	        		
	        		//Actualizamos el valor de las plazas libres a traves de la variable decrementada
	        		String decrementarPlazas  = ("UPDATE viajes SET nPlazasLibres = ? " +
	        									 "WHERE idViaje = ?");
	        		PreparedStatement decrementarPlazasSt = con.prepareStatement(decrementarPlazas);
	        		decrementarPlazasSt.setInt(1, plazasLibres);
	        		decrementarPlazasSt.setInt(2, idDelViaje);
	        		decrementarPlazasSt.executeUpdate();
	        		
	        		//Mostramos mensaje de éxito
	        		LOGGER.info("Billete comprado con éxito!! Precio total: " + (precioBillete * nroPlazas));
	        	}else {
	        		throw new SQLException("No hay plazas suficientes para este viaje", ".", CompraBilleteTrenException.NO_PLAZAS);
	        	}
	        	
	        }else {
	            // Si no se encontró ningún resultado, lanzar una excepción con el código de error correspondiente
	        	throw new SQLException("El viaje no existe", ".", CompraBilleteTrenException.NO_EXISTE_VIAJE);
	        }
	}
}
