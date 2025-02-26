package net.darkness.mosaic.service;

import net.darkness.mosaic.entity.*;
import net.darkness.mosaic.exception.*;
import net.darkness.mosaic.utils.ImageUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.*;

import static net.darkness.mosaic.MosaicApplication.*;


@Service
public class ConvertService {
    private final AccessCodeService accessCodeService;

    public ConvertService(AccessCodeService accessCodeService) {
        this.accessCodeService = accessCodeService;
    }

    private static ImageUtils.BlockResult convertImage(MultipartFile imageFile, String size, String palette,
                                                       Path originalPath, Path convertedPath, Path visualPath) {
        try {
            Files.write(originalPath, imageFile.getBytes());
            return ImageUtils.convertToMosaica(new ByteArrayInputStream(imageFile.getBytes()), size, palette, convertedPath, visualPath);
        } catch (IOException e) {
            throw new FailedToWriteImageException(e);
        }
    }

    private static <T> List<List<T>> unflattenGrid(int w, int h, List<T> flatGrid) {
        int tileNumber = 0;
        List<List<T>> columns = new ArrayList<>();
        for (int i = 0; i < w; i++) {
            List<T> column = new ArrayList<>();
            for (int j = 0; j < h; j++) {
                column.add(flatGrid.get(tileNumber));
                tileNumber++;
            }
            columns.add(column);
        }
        return columns;
    }

    private String newRandomFilename(String filename) {
        return UUID.randomUUID() + "." + FilenameUtils.getExtension(filename);
    }

    @Transactional
    public ImageResult tryConvertImageUsingCodeId(String uuid, MultipartFile imageFile, String size, String pallete) {
        AccessCode accessCode = accessCodeService.getAccessCode(uuid);
        if (accessCode == null)
            return null;

        if (accessCode.getState() == AccessCode.State.USED)
            return null;

        String originalFileName = newRandomFilename(imageFile.getOriginalFilename());
        Path originalPath = Path.of(IMAGES_DIRECTORY, originalFileName);

        String convertedFileName = newRandomFilename(imageFile.getOriginalFilename());
        Path convertedPath = Path.of(IMAGES_DIRECTORY, convertedFileName);

        String visualFileName = newRandomFilename(imageFile.getOriginalFilename());
        Path visualPath = Path.of(IMAGES_DIRECTORY, visualFileName);

        var result = convertImage(imageFile, size, pallete, originalPath, convertedPath, visualPath);

        accessCode.setState(AccessCode.State.CREATED);
        accessCode.setOriginalImageName(originalFileName);
        accessCode.setGeneratedImageName(convertedFileName);
        accessCode.setVisualImageName(visualFileName);

        accessCode.setPalette(pallete);
        accessCode.setVerticalBlocks(result.verticalBlocks());
        accessCode.setHorizontalBlocks(result.horizontalBlocks());
        accessCode.getBlocks().addAll(result.blocks());

        return new ImageResult(
                originalFileName,
                convertedFileName,
                visualFileName,
                accessCode.getCode()
        );
    }

    public ImageResult getImagesForCodeId(String uuid) {
        AccessCode accessCode = accessCodeService.getAccessCode(uuid);
        if (accessCode == null)
            throw new CodeNotFoundException();

        if (accessCode.getState() != AccessCode.State.USED)
            throw new ImagesForCodeNotFoundException();

        return new ImageResult(
                accessCode.getOriginalImageName(),
                accessCode.getGeneratedImageName(),
                accessCode.getVisualImageName(),
                accessCode.getCode()
        );
    }

    public Optional<BlockResult> getColorsForBlock(String code, int blockNumber) {
        return accessCodeService.getAccessCodeByCode(code)
                .map(accessCode -> {
                    List<ImageBlock> blocks = accessCode.getBlocks();
                    ImageBlock block = blocks.get(blockNumber);
                    List<List<Integer>> blockColors = unflattenGrid(
                            block.getWidth(),
                            block.getHeight(),
                            block.getBlockColors()
                    );
                    return new BlockResult(blockColors, accessCode.getPalette(), blocks.size());
                });
    }

    public List<List<Integer>> getBlocksForCode(String code) {
        return accessCodeService.getAccessCodeByCode(code).map(accessCode -> unflattenGrid(
                accessCode.getHorizontalBlocks(),
                accessCode.getVerticalBlocks(),
                accessCode.getBlocks().stream().map(ImageBlock::getBlockNumber).toList()
        )).orElseGet(Collections::emptyList);
    }

    public String getPaletteCss(String palette) {
        List<Color> colors = ImageUtils.getColorsForPalette(palette);
        StringBuilder css = new StringBuilder();
        css.append("""
                .color-0 {
                    background-color: white;
                }
                                
                """);
        for (int i = 0; i < colors.size(); i++) {
            Color color = colors.get(i);
            css.append("""
                    .color-%d {
                        background-color: rgb(%d,%d,%d);
                    }
                                        
                    """.formatted(i + 1, color.getRed(), color.getGreen(), color.getBlue()));
        }
        return css.toString();
    }

    public record BlockResult(List<List<Integer>> blockColors, String palette, int totalBlocks) {

    }

    public record ImageResult(String originalName, String convertedName, String visualName, String code) {
    }
}
