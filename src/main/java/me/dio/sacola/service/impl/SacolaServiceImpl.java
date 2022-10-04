package me.dio.sacola.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dio.sacola.enumeration.FormaPagamento;
import me.dio.sacola.model.Item;
import me.dio.sacola.model.Restaurante;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;
import me.dio.sacola.resource.dto.ItemDto;
import me.dio.sacola.service.SacolaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SacolaServiceImpl implements SacolaService {
  public static final String ERROR_RUNTIME_EXCEPTION_IN_METHOD = "Error RuntimeException in Method";
  public static final String STARTING_METHOD = "Starting Method";
  @Autowired
  private final SacolaRepository sacolaRepository;
  @Autowired
  private final ProdutoRepository produtoRepository;

  @Override
  public Item incluirItemNaSacola(final ItemDto itemDto) {
    String methodName = "incluirItemNaSacola";
    log.info(STARTING_METHOD + methodName);

    Sacola sacola = verSacola(itemDto.getSacolaId());

    if (sacola.isFechada()) {
      log.error(ERROR_RUNTIME_EXCEPTION_IN_METHOD + methodName);
      throw new RuntimeException("Esta sacola está fechada.");
    }

    Item itemParaSerInserido = Item.builder()
        .quantidade(itemDto.getQuantidade())
        .sacola(sacola)
        .produto(produtoRepository.findById(itemDto.getProdutoId()).orElseThrow(
            () -> {
              log.error(ERROR_RUNTIME_EXCEPTION_IN_METHOD + methodName);
              throw new RuntimeException("Esse produto não existe!");
            }
        ))
        .build();

    List<Item> itensDaScola = sacola.getItens();
    if (itensDaScola.isEmpty()) {
      itensDaScola.add(itemParaSerInserido);
    } else {
      Restaurante restauranteAtual = itensDaScola.get(0).getProduto().getRestaurante();
      Restaurante restauranteDoItemParaAdicionar = itemParaSerInserido.getProduto().getRestaurante();
      if (restauranteAtual.equals(restauranteDoItemParaAdicionar)) {
        itensDaScola.add(itemParaSerInserido);
      } else {
        log.error(ERROR_RUNTIME_EXCEPTION_IN_METHOD + methodName);
        throw new RuntimeException("Não é possível adicionar produtos de restaurantes diferentes. Feche a sacola ou esvazie.");
      }
    }

    List<Double> valorDosItens = new ArrayList<>();
    for (Item itemDaSacola: itensDaScola) {
      double valorTotalItem =
          itemDaSacola.getProduto().getValorUnitario() * itemDaSacola.getQuantidade();
      valorDosItens.add(valorTotalItem);
    }

    double valorTotalSacola = valorDosItens.stream()
        .mapToDouble(valorTotalDeCadaItem -> valorTotalDeCadaItem)
        .sum();

    sacola.setValorTotal(valorTotalSacola);
    sacolaRepository.save(sacola);
    return itemParaSerInserido;
  }

  @Override
  public Sacola verSacola(final Long id) {
    String methodName = "verSacola";
    log.info(STARTING_METHOD + methodName);
    return sacolaRepository.findById(id).orElseThrow(
        () -> {
          log.error(ERROR_RUNTIME_EXCEPTION_IN_METHOD + methodName);
          throw new RuntimeException("Essa sacola não existe!");
        }
    );
  }

  @Override
  public Sacola fecharSacola(final Long id, final int numeroformaPagamento) {
    String methodName = "fecharSacola";
    log.info(STARTING_METHOD + methodName);

    Sacola sacola = verSacola(id);
    if (sacola.getItens().isEmpty()) {
      throw new RuntimeException("Inclua ítens na sacola!");
    }
    FormaPagamento formaPagamento =
        numeroformaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUINETA;
    sacola.setFormaPagamento(formaPagamento);
    sacola.setFechada(true);
    return sacolaRepository.save(sacola);
  }
}
