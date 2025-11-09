# Image2Pdf

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Image2Pdf is a simple Android application that allows you to select images from your device, organize them, and convert them into a single PDF file. The app is designed to be offline-first and private, processing all files directly on your device.

## Features

*   **Select Multiple Images**: Pick one or more images from your device's gallery.
*   **Preview and Reorder**: View your selected images in a list. Drag and drop to reorder them before creating the PDF.
*   **Image Details**: View properties of each selected image, including name, size, and location.
*   **Remove Images**: Easily remove unwanted images from your selection.
*   **Create PDF**: Convert the ordered images into a PDF file. You can specify a name for your PDF.
*   **Organized Storage**: All generated PDFs are saved in a dedicated `Image2Pdf` folder inside your device's `Downloads` directory.
*   **View PDFs**: Access and view all the PDFs you've created directly from the app.
*   **About Section**: Displays app version, installation date, and author information.

## How to Use

1.  Tap the "Select Images" button to choose images from your device.
2.  On the preview screen, you can:
    *   Drag and drop images to change their order.
    *   Tap the delete icon to remove an image.
    *   Tap the info icon to see file details.
3.  Tap the "Convert To PDF" button.
4.  Enter a name for your PDF file and tap "Save."
5.  After the PDF is created, you can find it in the `Downloads/Image2Pdf` folder on your device.
6.  Tap the "View PDF Files" button on the main screen to see a list of all your created PDFs and open them.

## Technology Stack

*   [Kotlin](https://kotlinlang.org/)
*   [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   [Material 3](https://m3.material.io/)
*   [Coil](https://coil-kt.github.io/coil/) for image loading
*   [Reorderable](https://github.com/burnout-crew/reorderable) for the reorderable list

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
