#version 150

// The plasma of the Tokamak Fusion Reactor as a volume, seen from inside or out. A ring of hot gas runs round the vacuum
// vessel, a rounded octagon 37 blocks across: a violet halo, a hot magenta body, a white-hot core, with real turbulence
// in it, held in strands of field that twist round the ring and drift along it. As the drift climbs it kinks, flares
// at the edge, throws arcs of lightning out toward the walls and shades to orange and red, and a scram turns it red as it
// dies. The themes run their own gradient of colour round the ring, and the phoenix of the logo flies round it in a
// trail of fire.
// The ring is found by its exact distance field, a rounded octagon, so the march takes long strides far from the tube and
// fine ones inside it, with no wasted steps on empty air.

uniform float Time;
uniform float Power;
uniform float Seed;
uniform float Scale;
uniform float Detail;    // 0.3 to 1, how fine the steps inside the plasma are
uniform vec3 Centre;     // the middle of the ring, relative to the camera
uniform vec3 BoxHalf;    // half the size of the box, in blocks
uniform vec2 Right;      // the way the machine's right points, in the world (x, z)
uniform vec2 Front;      // the way it faces
uniform vec3 ColorA;     // the body of the plasma
uniform vec3 ColorB;     // its white-hot core
uniform vec3 ColorC;     // the halo
uniform vec3 ColorD;     // the strands of field
uniform vec3 Stop0;
uniform vec3 Stop1;
uniform vec3 Stop2;
uniform vec3 Stop3;
uniform vec3 Stop4;
uniform vec3 Stop5;
uniform vec4 P0;         // x how thick the plasma is, y how hard it shakes, z alarm 0..1, w how unstable 0..1
uniform vec4 P1;         // x how many strands, y how many turns they make, z the radius of the strands (times thickness), w how strongly the field holds
uniform vec4 P2;         // x how many cycles of the theme round the ring, y its speed, z how many colours it has (0 for none), w 1 for the phoenix

in vec3 wpos;
out vec4 fragColor;

const float TAU = 6.2831853;
const vec3 CORE_C = vec3(1.0, 0.86, 0.96);
const vec3 WARM = vec3(1.0, 0.58, 0.2);
const vec3 ALERT = vec3(1.0, 0.22, 0.18);

float h11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float h31(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float n3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(h31(i), h31(i + vec3(1.0, 0.0, 0.0)), f.x),
                   mix(h31(i + vec3(0.0, 1.0, 0.0)), h31(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
               mix(mix(h31(i + vec3(0.0, 0.0, 1.0)), h31(i + vec3(1.0, 0.0, 1.0)), f.x),
                   mix(h31(i + vec3(0.0, 1.0, 1.0)), h31(i + vec3(1.0, 1.0, 1.0)), f.x), f.y), f.z);
}

vec2 boxHit(vec3 ro, vec3 rd, vec3 h) {
    vec3 inv = 1.0 / rd;
    vec3 t0 = (-h - ro) * inv;
    vec3 t1 = (h - ro) * inv;
    vec3 tmin = min(t0, t1);
    vec3 tmax = max(t0, t1);
    return vec2(max(max(tmin.x, tmin.y), tmin.z), min(min(tmax.x, tmax.y), tmax.z));
}

float smax(float a, float b, float k) {
    float h = max(k - abs(a - b), 0.0) / k;
    return max(a, b) + h * h * k * 0.25;
}

// the signed distance to the vacuum vessel's racetrack: a rounded octagon, negative inside
float sdTrack(vec2 p) {
    vec2 a = abs(p);
    float d1 = a.x - 18.5;
    float d2 = a.y - 18.5;
    float d3 = (a.x + a.y - 28.0) * 0.70710678;
    return smax(smax(d1, d2, 3.0), d3, 3.0);
}

vec3 stopAt(int i) {
    if (i == 0) return Stop0;
    if (i == 1) return Stop1;
    if (i == 2) return Stop2;
    if (i == 3) return Stop3;
    if (i == 4) return Stop4;
    return Stop5;
}

// the colour of a theme at a place round the ring, s from 0 to 1
vec3 themeColour(float s) {
    int n = int(P2.z + 0.5);
    float cycles = P2.x;
    float speed = P2.y;
    if (n >= 3) {
        float p = fract(s * cycles - Time * speed * 2.0) * float(n);
        int i = min(n - 1, int(p));
        float f = p - float(i);
        f = f * f * (3.0 - 2.0 * f);
        return mix(stopAt(i), stopAt((i + 1) % n), f);
    }
    float g = 0.5 + 0.5 * sin(TAU * (s * cycles - Time * speed * 2.0));
    return mix(Stop0, Stop1, g);
}

uniform sampler2D DepthSampler;   // the depth of everything drawn so far, so rays stop at the first block in the way
uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

// how far along the ray (in blocks) the nearest thing already drawn is, or a huge number if there is nothing
float sceneDistance(vec3 rayWorld) {
    float d = texelFetch(DepthSampler, ivec2(gl_FragCoord.xy), 0).r;
    if (d >= 0.99999) return 1.0e9;
    float zn = d * 2.0 - 1.0;
    float zv = -ProjMat[3][2] / (zn + ProjMat[2][2]);       // how far in front of the camera, negative
    vec3 rv = mat3(ModelViewMat) * rayWorld;
    return rv.z < -1.0e-4 ? zv / rv.z : 1.0e9;
}

void main() {
    vec3 rd = normalize(wpos);
    vec3 ro = -Centre;
    float tw = length(wpos);
    vec2 hit = boxHit(ro, rd, BoxHalf);
    if (hit.y <= 0.0 || hit.x >= hit.y) discard;
    float face = hit.x > 0.0 ? hit.x : hit.y;
    if (abs(tw - face) > 0.15) discard;

    float fat = P0.x;
    float shake = P0.y;
    float alarm = P0.z;
    float drift = P0.w;
    float strands = P1.x;
    float twist = P1.y;
    float rStrand = P1.z * fat;
    float hold = P1.w;
    bool themed = P2.z > 0.5;
    float alarmK = alarm > 0.0 ? alarm * (0.4 + 0.4 * (0.5 + 0.5 * sin(Time * 11.0))) : 0.0;

    float rOut = 3.4 * max(fat, rStrand) + 0.8 + shake * 1.5;       // the outer edge of anything that glows
    float t0 = max(hit.x, 0.0);
    float t1 = min(hit.y, sceneDistance(rd) / 1.0);
    if (t1 <= t0) discard;
    float jit = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453);
    float minStep = max(0.12, rOut * 0.14) / max(Detail, 0.3);
    vec3 acc = vec3(0.0);

    float t = t0 + jit * minStep;
    for (int i = 0; i < 110; i++) {
        if (t > t1) break;
        vec3 p = ro + rd * t;
        // the racetrack lies in the machine's own axes
        vec2 uv = vec2(dot(p.xz, Right), dot(p.xz, Front));
        float sd = sdTrack(uv);
        float s = fract((-atan(uv.y, uv.x) - 2.04) / TAU);

        // the ring is kinked sideways and up by the drift
        float kinkSide = shake * (sin(s * TAU * 3.0 + Time * 1.7) * 0.6 + sin(s * TAU * 7.0 - Time * 2.9) * 0.4);
        float kinkUp = shake * 0.8 * sin(s * TAU * 5.0 + Time * 2.3);
        float lateral = sd - kinkSide;
        float y = p.y - kinkUp;
        float rho = sqrt(lateral * lateral + y * y);

        float dist = rho - rOut;
        if (dist > 0.0) {
            t += max(dist * 0.9, minStep);
            continue;
        }

        // inside the glow: the colours of this place
        vec3 mid = ColorA;
        vec3 halo = ColorC;
        vec3 core = ColorB;
        vec3 field = ColorD;
        if (themed) {
            vec3 c = themeColour(s);
            halo = mix(c, vec3(0.5, 0.35, 1.0), 0.3);
            mid = c;
            core = mix(CORE_C, c, 0.3);
            field = mix(Stop1, c, 0.4);
            if (alarmK > 0.0) {
                mid = mix(mid, ALERT, alarmK);
                halo = mix(halo, ALERT, alarmK);
                core = mix(core, WARM, alarmK);
            }
        }

        // turbulence in the plasma, dragged along the ring
        float ang = atan(uv.y, uv.x);
        float turb = n3(vec3(ang * 6.0 - Time * 1.3, lateral * 1.4 / max(fat, 0.2), y * 1.4 / max(fat, 0.2)) + vec3(Seed, 0.0, 0.0));
        float flick = 0.88 + 0.12 * sin(Time * 9.0 + s * 60.0);
        float f2 = fat * fat;
        float rho2 = rho * rho;
        vec3 e = vec3(0.0);
        e += halo * exp(-rho2 / (f2 * 2.6)) * 0.11;
        e += mid * exp(-rho2 / (f2 * 0.9)) * (0.3 + 0.35 * turb) * flick;
        e += core * exp(-rho2 / (f2 * 0.16)) * (0.8 + 0.5 * turb) * flick;

        // strands of field twisting round the ring and drifting along it
        float psi = atan(y, lateral);
        float spin = twist * TAU * s + Time * (0.6 + 1.2 * drift);
        float strand = pow(0.5 + 0.5 * cos(strands * (psi - spin / max(strands, 1.0))), 6.0);
        float onShell = exp(-pow((rho - rStrand) / (0.16 * fat + 0.03), 2.0));
        vec3 fieldC = field;
        if (alarm > 0.0) fieldC = mix(fieldC, ALERT, alarm * 0.7);
        e += fieldC * strand * onShell * (0.45 + 1.1 * clamp(hold * 0.55, 0.15, 1.0)) * 1.3;

        // as the drift climbs the edge flares and arcs of lightning leap out toward the walls
        float flare = pow(n3(vec3(ang * 9.0 + Time * 3.0, lateral * 0.6, y * 0.6)), 4.0) * smoothstep(0.2, 0.9, drift);
        e += mix(mid, WARM, drift) * flare * exp(-pow((rho - fat * 1.6) / (fat * 0.9), 2.0)) * 1.8;
        float arc = pow(n3(vec3(ang * 12.0 - Time * 6.0, rho * 2.0, Time)), 8.0) * smoothstep(0.55, 1.0, drift);
        e += mix(ColorD, WARM, 0.5) * arc * exp(-rho2 / (f2 * 7.0)) * 2.4;

        // the phoenix of the logo: a bright head flying round the ring, trailing fire
        if (P2.w > 0.5) {
            float head = fract(Time * 0.07);
            float behind = fract(head - s + 1.0);
            float f = behind < 0.32 ? 1.0 - behind / 0.32 : 0.0;
            e += mix(Stop0, vec3(1.0, 0.95, 0.9), f * f) * f * f * exp(-rho2 / (f2 * 1.9)) * (1.6 + 1.5 * step(behind, 0.02));
        }

        // a thicker tube adds up more light along a ray, so it is dimmed to keep the same brightness
        float stepLen = max(minStep, rho * 0.2);
        acc += e * stepLen * clamp(0.6 / max(fat, 0.2), 0.12, 1.6);
        t += stepLen;
    }

    fragColor = vec4(acc * Power, 1.0);
}
