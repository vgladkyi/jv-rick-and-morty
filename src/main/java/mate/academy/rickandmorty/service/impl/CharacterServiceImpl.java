package mate.academy.rickandmorty.service.impl;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mate.academy.rickandmorty.dto.external.CharacterResponseDto;
import mate.academy.rickandmorty.dto.internal.CharacterDto;
import mate.academy.rickandmorty.mapper.CharacterMapper;
import mate.academy.rickandmorty.model.Character;
import mate.academy.rickandmorty.repository.CharacterRepository;
import mate.academy.rickandmorty.service.CharacterService;
import mate.academy.rickandmorty.service.CharactersClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterServiceImpl implements CharacterService {
    private static Long maxId;
    private static boolean isDbUpdated;
    private final CharactersClient client;
    private final CharacterMapper mapper;
    private final CharacterRepository repository;

    @Override
    public CharacterDto getRandom() {
        if (!isDbUpdated) {
            getDataFromApi();
        }
        Random random = new Random();
        long randomLong = random.nextLong(maxId + 1);
        Character character = repository.findById(randomLong)
                .orElseThrow(() -> new RuntimeException("Error while retrieving character"));
        return mapper.toDto(character);
    }

    @Override
    public List<CharacterDto> getAllByName(String name) {
        if (!isDbUpdated) {
            getDataFromApi();
        }
        return repository.findCharactersByNameContainsIgnoreCase(name)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public void getDataFromApi() {
        List<CharacterResponseDto> charactersList = client.getAllCharacters();
        repository.saveAll(client.getAllCharacters()
                .stream()
                .map(mapper::toModel)
                .collect(Collectors.toList()));
        maxId = charactersList.get(charactersList.size() - 1).id();
        isDbUpdated = true;
    }
}
