# Approved PIET Canteen UI assets

The approved screen references are stored in `docs/approved-ui-reference/`.

Runtime image resources are bundled inside the Java source package so the application does not require manual image setup:

- `src/canteen/canteen_bg.jpg` — login cafeteria background
- `src/canteen/brand_header.jpg` — brand header image
- `src/canteen/app_icon.png` — application/window icon
- `src/canteen/food_images/*.jpg` — food item fallback images used by menu and billing cards

The original editable food-image folder `food_images/` is also included for convenience. If a database item has a custom image path, that custom path may be used instead of the bundled fallback.

## Rebuild

Open the project in NetBeans, choose **Clean and Build**, then **Run**. The included `build/` and `dist/` folders are generated output; NetBeans can regenerate them.
