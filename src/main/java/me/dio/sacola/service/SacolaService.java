package me.dio.sacola.service;

import me.dio.sacola.model.Item;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.resource.dto.ItemDto;

public interface SacolaService {
  Item incluirItemNaSacola(final ItemDto itemDto);
  Sacola verSacola(final Long id);
  Sacola fecharSacola(final Long id, final int formaPagamento);
}
