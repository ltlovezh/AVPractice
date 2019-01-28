precision mediump float;
uniform sampler2D vTexture;

varying vec2 textureCoordinate;
varying vec4 vColor;
void main() {
  gl_FragColor = vColor;
}
