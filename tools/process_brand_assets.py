#!/usr/bin/env python3
"""Prepare generated EasyBangumi brand art for Android resources."""

from __future__ import annotations

import argparse
import math
from pathlib import Path

from PIL import Image, ImageEnhance, ImageOps


def remove_green_screen(image: Image.Image) -> Image.Image:
    """Remove a saturated green background while preserving antialiased edges."""
    rgba = image.convert("RGBA")
    pixels = []
    for red, green, blue, _ in rgba.get_flattened_data():
        dominance = green - max(red, blue)
        if green > 105 and dominance > 18:
            alpha = round(255 * (1 - min(1.0, max(0.0, (dominance - 18) / 62))))
            if alpha < 255:
                green = min(green, max(red, blue) + 12)
            pixels.append((red, green, blue, alpha))
        else:
            pixels.append((red, green, blue, 255))
    rgba.putdata(pixels)
    return rgba


def resize_square(image: Image.Image, size: int) -> Image.Image:
    return image.resize((size, size), Image.Resampling.LANCZOS)


def save_rgba(source: Path, destination: Path, size: int) -> None:
    image = remove_green_screen(Image.open(source))
    destination.parent.mkdir(parents=True, exist_ok=True)
    resize_square(image, size).save(destination, optimize=True)


def save_alpha(source: Path, destination: Path, size: int) -> None:
    """Resize an already-matted RGBA master without discarding its transparency."""
    image = Image.open(source).convert("RGBA")
    destination.parent.mkdir(parents=True, exist_ok=True)
    resize_square(image, size).save(destination, optimize=True)


def save_logo(source: Path, destinations: list[Path], size: int) -> None:
    image = resize_square(Image.open(source).convert("RGB"), size)
    for destination in destinations:
        destination.parent.mkdir(parents=True, exist_ok=True)
        image.save(destination, optimize=True)


def make_loading_gif(source: Path, destination: Path, size: int) -> None:
    base = resize_square(remove_green_screen(Image.open(source)), size)
    canvas_size = size + 20
    frames: list[Image.Image] = []

    frame_count = 16
    for index in range(frame_count):
        phase = (index / frame_count) * math.tau
        scale = 0.975 + 0.018 * (1 + math.sin(phase)) / 2
        angle = 1.4 * math.sin(phase)
        bob = round(2.5 * math.sin(phase))
        frame_size = max(1, round(size * scale))
        animated = base.resize((frame_size, frame_size), Image.Resampling.LANCZOS)
        animated = animated.rotate(
            angle,
            resample=Image.Resampling.BICUBIC,
            expand=True,
        )

        if index in (2, 3, 4, 12, 13, 14):
            animated = ImageEnhance.Brightness(animated).enhance(1.035)

        frame = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
        x = (canvas_size - animated.width) // 2
        y = (canvas_size - animated.height) // 2 + bob
        frame.alpha_composite(animated, (x, y))
        frames.append(frame)

    destination.parent.mkdir(parents=True, exist_ok=True)
    palette_frames = []
    for frame in frames:
        alpha = frame.getchannel("A")
        paletted = frame.convert("RGB").quantize(
            colors=95,
            method=Image.Quantize.MEDIANCUT,
        )
        palette = paletted.getpalette()
        palette[255 * 3 : 255 * 3 + 3] = [0, 0, 0]
        paletted.putpalette(palette)
        transparent_pixels = alpha.point(lambda value: 255 if value < 96 else 0)
        paletted.paste(255, mask=transparent_pixels)
        palette_frames.append(paletted)
    palette_frames[0].save(
        destination,
        save_all=True,
        append_images=palette_frames[1:],
        duration=80,
        loop=0,
        disposal=2,
        transparency=255,
        optimize=False,
    )


def fit_on_surface(
    image: Image.Image,
    size: tuple[int, int],
    background: tuple[int, int, int],
    padding: int = 24,
) -> Image.Image:
    surface = Image.new("RGB", size, background)
    contained = ImageOps.contain(
        image.convert("RGBA"),
        (size[0] - padding * 2, size[1] - padding * 2),
        Image.Resampling.LANCZOS,
    )
    x = (size[0] - contained.width) // 2
    y = (size[1] - contained.height) // 2
    surface.paste(contained, (x, y), contained)
    return surface


def make_qa_comparison(
    source: Path,
    runtime: Path,
    settings: Path,
    project_root: Path,
    destination: Path,
) -> None:
    ivory = (255, 249, 245)
    canvas = Image.new("RGB", (1800, 1400), ivory)
    source_panel = fit_on_surface(Image.open(source), (600, 1360), ivory, 12)
    canvas.paste(source_panel, (10, 20))

    asset_paths = [
        project_root / "app/src/main/res/mipmap-xxhdpi/logo_new.png",
        project_root / "app/src/main/res/drawable/empty_bocchi.png",
        project_root / "app/src/main/assets/loading_ryo.gif",
        project_root / "app/src/main/res/drawable/error_ikuyo.png",
    ]
    for index, asset_path in enumerate(asset_paths):
        panel = fit_on_surface(Image.open(asset_path), (270, 310), ivory, 18)
        canvas.paste(panel, (640 + index * 282, 20))

    runtime_panel = fit_on_surface(Image.open(runtime), (1130, 520), ivory, 8)
    canvas.paste(runtime_panel, (640, 350))

    settings_image = Image.open(settings)
    settings_crop = settings_image.crop(
        (
            0,
            round(settings_image.height * 0.10),
            settings_image.width,
            round(settings_image.height * 0.38),
        )
    )
    settings_panel = fit_on_surface(settings_crop, (1130, 470), ivory, 8)
    canvas.paste(settings_panel, (640, 900))

    destination.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(destination, optimize=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--logo", type=Path, required=True)
    parser.add_argument("--empty", type=Path, required=True)
    parser.add_argument("--loading", type=Path, required=True)
    parser.add_argument("--error", type=Path, required=True)
    parser.add_argument("--verification", type=Path)
    parser.add_argument("--project-root", type=Path, required=True)
    parser.add_argument("--qa-source", type=Path)
    parser.add_argument("--qa-runtime", type=Path)
    parser.add_argument("--qa-settings", type=Path)
    parser.add_argument("--qa-output", type=Path)
    args = parser.parse_args()

    resources = args.project_root / "app/src/main"
    logo_destinations = [
        resources / "res/mipmap-xxhdpi/logo_new.png",
    ]
    save_logo(args.logo, logo_destinations, 512)
    save_rgba(args.empty, resources / "res/drawable/empty_bocchi.png", 384)
    save_rgba(args.error, resources / "res/drawable/error_ikuyo.png", 384)
    if args.verification is not None:
        save_alpha(
            args.verification,
            resources / "res/drawable-nodpi/search_verification.png",
            384,
        )
    make_loading_gif(
        args.loading,
        resources / "assets/loading_ryo.gif",
        256,
    )
    qa_inputs = (
        args.qa_source,
        args.qa_runtime,
        args.qa_settings,
        args.qa_output,
    )
    if all(qa_inputs):
        make_qa_comparison(
            args.qa_source,
            args.qa_runtime,
            args.qa_settings,
            args.project_root,
            args.qa_output,
        )


if __name__ == "__main__":
    main()
