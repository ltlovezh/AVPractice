attribute vec4 vPosition;
attribute vec2 vCoord;
attribute vec4 aColor;

uniform mat4 vMatrix;

varying vec2 textureCoordinate;
varying vec4 vColor;
void main() {
   gl_Position = vMatrix * vPosition;
   textureCoordinate = vCoord;
   vColor = aColor;
}

