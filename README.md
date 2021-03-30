# Blocks View
A custom widget library for android used to display blocks of codes similar to Sketchware and Scratch.

This project is very experimental, and is not suitable for production usage.

## Features
 - [x] Blocks
 - [x] Nested Blocks
 - [x] Parameter Blocks
 - [x] String, Integer, Boolean, and Other Fields
 - [x] Custom blocks
 - [x] Drag blocks
 - [ ] Drop blocks (TODO)

## Sample
<img src="screenshots/1.png"/>

## Using
We currently haven't published this library into any platforms yet because it's still in development, if you want to try it, just clone this project into your project directory, then create a new module, select gradle project, and select the folder that you cloned this repository into (make sure to pick `:lib`).

### Basic usage:
```
<com.openblocks.blocks.view.BlocksView
    android:id="@+id/blocks_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```
With this, you will be greeted with the demo blocks (as shown in sample).

