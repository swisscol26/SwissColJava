package dto;

public class CategoriaResultado {

    private final int categoriaId;
    private final String nombre;
    private final String tipo;

    public CategoriaResultado(
            int categoriaId,
            String nombre,
            String tipo) {

        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }
}