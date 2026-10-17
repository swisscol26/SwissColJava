package dao;

import conexion.Conexion;
import dto.CategoriaResultado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<CategoriaResultado> listar()
            throws SQLException {

        String sql = """
            SELECT category_id, name, type
            FROM category
            ORDER BY name
            """;

        List<CategoriaResultado> categorias =
            new ArrayList<>();

        try (
            Connection conexion = Conexion.conectar();
            PreparedStatement consulta =
                conexion.prepareStatement(sql);
            ResultSet filas = consulta.executeQuery()
        ) {

            while (filas.next()) {
                categorias.add(
                    new CategoriaResultado(
                        filas.getInt("category_id"),
                        filas.getString("name"),
                        filas.getString("type")
                    )
                );
            }
        }

        return categorias;
    }
}