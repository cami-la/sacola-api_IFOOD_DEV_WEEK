package me.dio.sacola.service;

import me.dio.sacola.model.Cliente;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;
import me.dio.sacola.service.impl.SacolaServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SacolaServiceImpl Test")
@ExtendWith(MockitoExtension.class)
public class SacolaServiceImplTest {

    private SacolaServiceImpl testClass;

    @Mock
    private SacolaRepository sacolaRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    @BeforeEach
    public void setup() {
        testClass = new SacolaServiceImpl (sacolaRepository,produtoRepository);
    }

    @Test
    @DisplayName("See package Products with success")
    public void seePackageSuccess(){
        Mockito.when(sacolaRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(thenReturnSacola()));

        Sacola sacola = testClass.verSacola(5L);
        Assertions.assertEquals( 32,sacola.getId());
    }

    @Test
    @DisplayName("See package Products with fail")
    public void seePackageFail(){
        Mockito.when(sacolaRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            testClass.verSacola(5L);
        });

        String expectedMessage = "Essa sacola não existe!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    /**
     * Helper
     */
    private Sacola thenReturnSacola(){
        return Sacola.builder()
                .cliente(thenReturnCliente())
                .id(32L)
                .fechada(true)
                .build();
    }
    private Cliente thenReturnCliente(){
        return Cliente.builder()
                .id(66L)
                .nome("Name Test")
                .build();
    }

}
