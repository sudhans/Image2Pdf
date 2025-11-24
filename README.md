# Image2Pdf

[![Build and Lint](https://github.com/sudhans/Image2Pdf/actions/workflows/build.yml/badge.svg)](https://github.com/sudhans/Image2Pdf/actions/workflows/build.yml)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Image2Pdf is a simple Android application that allows you to select images from your device, organize them, and convert them into a single PDF file. The app is designed to be offline-first and private, processing all files directly on your device.

## Features

*   **Select Multiple Images**: Pick one or more images from your device's gallery.
*   **Preview and Manage Images**: View your selected images in a list. Remove any unwanted images by tapping the delete icon.
*   **Image Details**: View properties of each selected image, including its name, size, and location.
*   **Flexible PDF Page Sizing**: Take full control over your PDF output with multiple page-sizing options:
    *   **A4 - Print Friendly**: Automatically scales each image (up or down) to best fit a standard A4 page.
    *   **A4 - Scale Down**: Fits larger images to an A4 page while keeping smaller images at their original size.
    *   **A4 - No Scaling**: Centers each image on an A4 page without applying any scaling.
    *   **A4 - Grid**: Intelligently arranges multiple images on a single A4 page, creating a compact grid and saving space.
    *   **Image Size**: Creates a PDF where each page's size matches the original image's dimensions.
*   **Organized Storage**: All generated PDFs are saved in a dedicated `Image2Pdf` folder inside your device's `Downloads` directory.
*   **View and Manage PDFs**: Access and view all the PDFs you've created directly from the app, sorted by creation date.
*   **Share Your App**: Easily share a link to the app with others via the overflow menu.
*   **About Section**: Displays app version, installation date, author information, and a link to the source code.

## How to Use

1.  Tap the "Select Images" button to choose images from your device.
2.  On the preview screen, you can:
    *   Tap the delete icon to remove an image.
    *   Tap the info icon to see file details.
3.  Go to "Settings" from the overflow menu to select your desired PDF page option.
4.  Tap the "Convert To PDF" button.
5.  Enter a name for your PDF file and tap "Save."
6.  After the PDF is created, you can find it in the `Downloads/Image2Pdf` folder on your device.
7.  Tap the "View PDF Files" button on the main screen to see a list of all your created PDFs and open them.

## Technology Stack

*   [Kotlin](https://kotlinlang.org/)
*   [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   [Material 3](https://m3.material.io/)
*   [Coil](https://coil-kt.github.io/coil/) for image loading

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
