package pruebas;

import com.google.gson.Gson;
import services.CategoriaService;

public class CategoriaConsultaPrueba {

    public static void main(String[] args) throws Exception {

        System.out.println("Consultando categorías...");

        var categorias = new CategoriaService().listar();

        System.out.println(
            "Categorías encontradas: " + categorias.size()
        );

        String json = new Gson().toJson(categorias);

        System.out.println(json);
    }
}