package services;

import dao.CategoriaDAO;
import dto.CategoriaResultado;

import java.sql.SQLException;
import java.util.List;

public class CategoriaService {

    private final CategoriaDAO categoriaDAO =
        new CategoriaDAO();

    public List<CategoriaResultado> listar()
            throws SQLException {

        return categoriaDAO.listar();
    }
}