package me.dio.sacola.service.impl;

import lombok.RequiredArgsConstructor;
import me.dio.sacola.enumeration.FormaPagamento;
import me.dio.sacola.model.Item;
import me.dio.sacola.model.Produto;
import me.dio.sacola.model.Restaurante;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.repository.ItemRepository;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;
import me.dio.sacola.resource.dto.ItemDto;
import me.dio.sacola.service.SacolaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SacolaServiceImpl implements SacolaService {
  private final SacolaRepository sacolaRepository;
  private final ProdutoRepository produtoRepository;
  private final ItemRepository itemRepository;

  @Override
  public Item incluirItemNaSacola(ItemDto itemDto) {
    Sacola sacola = verSacola(itemDto.getSacolaId());
    if (sacola.isFechada()) {
      throw new RuntimeException("Não é possível adicionar mais itens nesta sacola.");
    }
    Produto produto = getProduto(itemDto);
    Item item = criarNovoITem(itemDto, sacola, produto);
    List<Item> itens = sacola.getItens();
    if(itens.isEmpty()){
      itens.add(item);
      sacolaRepository.save(sacola);
    } else {
      Restaurante restauranteSacola = itens.get(0).getProduto().getRestaurante();
      Restaurante restauranteItemAtual = item.getProduto().getRestaurante();
      if(restauranteSacola.equals(restauranteItemAtual)) {
        itens.add(item);
        sacolaRepository.save(sacola);
      } else {
        throw new RuntimeException("Você só pode adicionar itens de um restaurante por vez.");
      }
    }
    calculaValorTotalSacola(sacola, item, itens);
    sacolaRepository.save(sacola);
    return itemRepository.save(item);
  }

  private static Item criarNovoITem(ItemDto itemDto, Sacola sacola, Produto produto) {
    return Item.builder()
        .sacola(sacola)
        .quantidade(itemDto.getQuantidade())
        .produto(produto)
        .build();
  }

  private Produto getProduto(ItemDto itemDto) {
    return produtoRepository.findById(itemDto.getProdutoId()).orElseThrow(
        () -> {
          throw new RuntimeException("Esse produto não existe!");
        }
    );
  }

  private static void calculaValorTotalSacola(Sacola sacola, Item item, List<Item> itens) {
    Double valorTotalSacola = itens.stream()
        .map(itemSacola -> itemSacola.getProduto().getValorUnitario() * item.getQuantidade())
        .toList()
        .stream()
        .reduce(0.0, Double::sum);
    sacola.setValorTotal(valorTotalSacola);
  }

  @Override
  public Sacola verSacola(Long id) {
    return sacolaRepository.findById(id).orElseThrow(
        () -> {
          throw new RuntimeException("Essa sacola não existe!");
        }
    );
  }

  @Override
  public Sacola fecharSacola(Long id, int numeroformaPagamento) {
    Sacola sacola = verSacola(id);
    if (sacola.getItens().isEmpty()) {
      throw new RuntimeException("Inclua ítens na sacola!");
    }
    /*if (numeroformaPagamento == 0) {
      sacola.setFormaPagamento(FormaPagamento.DINHEIRO);
    } else {
      sacola.setFormaPagamento(FormaPagamento.MAQUINETA);
    }*/
    FormaPagamento formaPagamento =
        numeroformaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUINETA;
    sacola.setFormaPagamento(formaPagamento);
    sacola.setFechada(true);
    return sacolaRepository.save(sacola);
  }
}
