package com.game.controller;

import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.models.Player;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // 1. получать список всех зарегистрированных игроков
    // @GetMapping будет "/players", так как адрес этого метода "/rest/players"
    // набрав "/rest/players" и сделав GET запрос мы попадем в этот метод
    @GetMapping("/players")
    // required = false,
    // если мы передаем в get запросе эти параметры, то эти параметры внедряются в эти переменны (name, title и т.д.)
    // если же мы в нашем запросе НЕ пишем эти параметры в url, то в этих переменных будет лежать null
    public List<Player> getPlayersList(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race,
                                       @RequestParam(value = "profession", required = false) Profession profession,
                                       @RequestParam(value = "after", required = false) Long after,
                                       @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "banned", required = false) Boolean banned,
                                       @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                       @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                       @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                       @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                       @RequestParam(value = "order", required = false) PlayerOrder order,
                                       @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                       @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        // Поиск по полям name и title происходить по частичному соответствию.
        // Например, если в БД есть игрок с именем «Камираж», а параметр name задан как «ир» -
        // такой игрок должен отображаться в результатах (Камираж).
        // pageNumber – параметр, который отвечает за номер отображаемой страницы при использовании пейджинга.
        // Нумерация начинается с нуля
        // pageSize – параметр, который отвечает за количество результатов на одной странице при пейджинге

        List<Player> playerList = playerService.getPlayerList(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel);
        List<Player> sortedPlayers = playerService.sortPlayers(playerList, order);

        return playerService.sortPage(sortedPlayers, pageNumber, pageSize);
    }

    // 7. получать количество игроков, которые соответствуют фильтрам
    @GetMapping("players/count")
    public Integer getPlayersCount(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "race", required = false) Race race,
                                   @RequestParam(value = "profession", required = false) Profession profession,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {

        return playerService.getPlayerList(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel).size();
    }

    // 2. создавать нового игрока
    @PostMapping ("/players")
    public Player createPlayer(@RequestBody Player player) {

        //public Player createPlayer() {
        // Мы не можем создать игрока, если:
        // - указаны не все параметры из Data Params (кроме banned);
        // - длина значения параметра “name” или “title” превышает размер соответствующего поля в БД (12 и 30 символов);
        // - значение параметра “name” пустая строка;
        // - опыт находится вне заданных пределов;
        // - “birthday”:[Long] < 0;
        // - дата регистрации находятся вне заданных пределов.
        // В случае всего вышеперечисленного необходимо ответить ошибкой с кодом 400.

        return playerService.createPlayer(player);
    }

    // 5. получать игрока по id;
    // с помощью аннотации @PathVariable мы извлечем этот id из url и получим к нему доступ внутри этого метода
    @GetMapping ("players/{id}")
    public Player getPlayer(@PathVariable(value = "id") Long id) {
        // Если игрок не найден в БД, необходимо ответить ошибкой с кодом 404.
        // Если значение id не валидное, необходимо ответить ошибкой с кодом 400.

        return playerService.findById(id);
    }

    // 3. редактировать характеристики существующего игрока
    @PostMapping("players/{id}")
    public Player updatePlayer(@PathVariable(value = "id") Long id,
                               // Значения параметров преобразуются в объявленный тип аргумента метода
                               @RequestBody Player player) {
        // Обновлять нужно только те поля, которые не null.
        // Если игрок не найден в БД, необходимо ответить ошибкой с кодом 404.
        // Если значение id не валидное, необходимо ответить ошибкой с кодом 400.

        return playerService.updatePlayer(id, player);
    }

    // 4. удалять игрока

    @DeleteMapping("/players/{id}")
    public void deletePlayer(@PathVariable(value = "id") Long id) {
        // Если игрок не найден в БД, необходимо ответить ошибкой с кодом 404.
        // Если значение id не валидное, необходимо ответить ошибкой с кодом 400.
        playerService.deleteById(id);
    }
}
