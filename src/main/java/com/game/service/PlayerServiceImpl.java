package com.game.service;


import com.game.controller.PlayerOrder;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;
import com.game.models.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlayerServiceImpl implements PlayerService{

    public final PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * получать список всех зарегистрированных игроков
     */
    @Override
    public List<Player> getPlayerList(String name, String title, Race race, Profession profession,
                                      Long after, Long before, Boolean banned, Integer minExperience,
                                      Integer maxExperience, Integer minLevel, Integer maxLevel) {

        List<Player> playerList = new ArrayList<>();

        playerRepository.findAll().forEach(player -> {
            // Поиск по полям name и title происходить по частичному соответствию.
            // Например, если в БД есть игрок с именем «Камираж», а параметр name задан как «ир»
            // - такой игрок должен отображаться в результатах (Камираж)
            if (name != null && !player.getName().contains(name))
                return;
            if (title != null && !player.getTitle().contains(title))
                return;
            if (race != null && player.getRace() != race) {
                return;
            }
            if (profession != null && player.getProfession() != profession) {
                return;
            }
            if (after != null) {
                Date afterDate = new Date(after);
                if (!player.getBirthday().after(afterDate)) {
                    return;
                }
            }
            if (before != null) {
                Date beforeDate = new Date(before);
                if (!player.getBirthday().before(beforeDate)) {
                    return;
                }
            }
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) {
                return;
            }
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) {
                return;
            }
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) {
                return;
            }
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) {
                return;
            }
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) {
                return;
            }

            playerList.add(player);
        });

        return playerList;
    }

    // проверяем, что длина значения параметра “name” и "title" не превышает размер 12 и 30 соответсвенно
    // значения параметров “name” и "title" не пустая строка

    private boolean isValidName(String name) {
        return name.length() <= 12 && !name.isEmpty();
    }

    private boolean isValidTitle(String title) {
        return title.length() <= 30 && !title.isEmpty();
    }

    // проверяем все ли параметры указаны
    private boolean isValidParams(Player player) {
        return player.getName() != null ||
                player.getTitle() != null ||
                player.getRace() != null ||
                player.getProfession() != null ||
                player.getBirthday() != null ||
                player.getExperience() != null;
    }

    // проверяем, что опыт не находится вне заданных пределов
    private boolean isValidExperience(Integer experience) {
        // Опыт персонажа. Диапазон значений 0..10,000,000
        return experience >= 0 && experience <= 10000000;
    }

    private boolean isValidDate(Date date) {
        if (date == null) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        Date after = new Date();
        Date before = new Date();
        calendar.set(1999, 11, 31);
        after = calendar.getTime();
        calendar.set(3000, 11, 31);
        before = calendar.getTime();
        //calendar = Calendar.getInstance();

        return  (date.before(before) && date.after(after));//&&
             //   calendar.get(Calendar.YEAR) >= 2000 && calendar.get(Calendar.YEAR) <= 3000;
    }

    // текущий уровень персонажа
    private Integer calculateLevel(Player player) {
        return ((int) ((Math.sqrt(2500 + 200 * player.getExperience())- 50) / 100));
    }

    // опыт необходимый для достижения следующего уровня
    private Integer calculateUntilNextLevel(Player player) {
        return 50 * (player.getLevel() + 1) * (player.getLevel() + 2) - player.getExperience();
    }

    /**
     * сохранять пользователя
     */
    // (пригодится для создания и редактирования нового игрока)
    @Override
    public Player createPlayer(Player player) {

        // проверяем все ли параметры указаны
        if (isValidParams(player)
                // проверяем длину значений имени,
                && isValidName(player.getName())
                // титула
                && isValidTitle(player.getTitle())
                // проверяем, что опыт не находится вне заданных пределов
                && isValidExperience(player.getExperience())
                // проверяем, что дата регистрации не находятся вне заданных пределов
                && isValidDate(player.getBirthday())) {

            player.setLevel(calculateLevel(player));
            player.setUntilNextLevel(calculateUntilNextLevel(player));
            return playerRepository.save(player);

        } else {
            throw new BadRequestException();
        }
    }

    /**
     * редактировать характеристики существующего игрока
     */
    @Override
    public Player updatePlayer(Long id, Player player) {
        // Если значение id не валидное, необходимо ответить ошибкой с кодом 400.
        if (id <= 0) {
            throw new BadRequestException();
        }

        // Если игрок не найден в БД, необходимо ответить ошибкой с кодом 404.
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException("player not found");
        }


        if (player.getBirthday() == null && player.getExperience() == null &&
                player.getName() == null && player.getLevel()== null&&
                player.getTitle() == null && player.getRace() == null && player.getProfession() == null &&
                player.getBanned() == null && player.getUntilNextLevel()==null
        ) return  playerRepository.findById(id).get();

        Player changedPlayer = playerRepository.findById(id).get();//getOne(id);

        // Обновлять нужно только те поля, которые не null
        if (player.getBirthday() != null) {
            if ( isValidDate(player.getBirthday()))
                changedPlayer.setBirthday(player.getBirthday());
            else  throw new BadRequestException();
        }
        if (player.getExperience() != null)
            if (isValidExperience(player.getExperience())) {
                changedPlayer.setExperience(player.getExperience());
            }else throw new BadRequestException();

        if (player.getName() != null) {
            changedPlayer.setName(player.getName());
        }
        if (player.getTitle() != null) {
            changedPlayer.setTitle(player.getTitle());
        }
        if (player.getRace() != null) {
            changedPlayer.setRace(player.getRace());
        }
        if (player.getProfession() != null) {
            changedPlayer.setProfession(player.getProfession());
        }

         if (player.getBanned() != null){
             changedPlayer.setBanned(player.getBanned());
         }

        changedPlayer.setLevel(calculateLevel(changedPlayer));
        changedPlayer.setUntilNextLevel(calculateUntilNextLevel(changedPlayer));


        return playerRepository.save(changedPlayer);
    }

    /**
     * удалять игрока
     */
    @Override
    public void deleteById(Long id) {
        // Если значение id не валидное, необходимо ответить ошибкой с кодом 400.
        if (id <= 0) {
            throw new BadRequestException();
        }
        // Если игрок не найден в БД, необходимо ответить ошибкой с кодом 404.
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException("playerNotFound");
        }
        playerRepository.deleteById(id);
    }

    /**
     * получать игрока по id
     */
    @Override
    public Player findById(Long id) {
        if (id <= 0) {
            throw new BadRequestException();
        }

        // Если игрок не найден в БД, необходимо ответить ошибкой с кодом 404.

        if (!playerRepository.existsById(id)) {
            throw new NotFoundException("playerNotFound");
        }
        // Если значение id не валидное, необходимо ответить ошибкой с кодом 400.


        return playerRepository.findById(id).get();
    }

    /**
     * сортировка списка в соответсвии с переданным параметром
     */
    // получать отфильтрованный список игроков в соответствии с переданными фильтрами
    @Override
    public List<Player> sortPlayers(List<Player> list, PlayerOrder order) {
        if (order != null) {
            switch (order) {
                case ID:
                    //list.sort(Comparator.comparing(Player -> Player.getId()));
                    list.sort(Comparator.comparing(Player::getId));
                    break;
                case NAME:
                    list.sort(Comparator.comparing(Player::getName));
                    break;
                case EXPERIENCE:
                    list.sort(Comparator.comparing(Player::getExperience));
                    break;
                case BIRTHDAY:
                    list.sort(Comparator.comparing(Player::getBirthday));
                    break;
            }
        }
        return list;
    }

    /**
     * сортировка страницы в соответсвии с переданными параметрами
     * pageNumber – параметр, который отвечает за номер отображаемой страницы при использовании пейджингаъ
     * pageSize – параметр, который отвечает за количество результатов на одной странице при пейджинге
     */
    // получать количество игроков, которые соответствуют фильтрам
    @Override
    public List<Player> sortPage(List<Player> list, Integer pageNumber, Integer pageSize) {
        // Если параметр pageNumber не указан – нужно использовать значение 0
        if (pageNumber == null) {
            pageNumber = 0;
        }
        // Если параметр pageSize не указан – нужно использовать значение 3
        if (pageSize == null) {
            pageSize = 3;
        }
        List<Player> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if ((pageNumber ==0 && i>=0 && i < pageSize) ||
                    ( i / pageSize == pageNumber && i % pageNumber >= 0 &&
                            i % pageNumber < pageSize)){
                result.add(list.get(i));
            }
        }
        return result;
    }
}
