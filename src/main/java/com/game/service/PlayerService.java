package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.models.Player;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


public interface PlayerService {

    Player updatePlayer(Long id, Player player);

    void deleteById(Long id);

    Player findById(Long id);

    Player createPlayer(Player player);

    List<Player> getPlayerList(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel);

    List<Player> sortPlayers(List<Player> playerList, PlayerOrder order);

    List<Player> sortPage(List<Player> sortedPlayers, Integer pageNumber, Integer pageSize);
}
