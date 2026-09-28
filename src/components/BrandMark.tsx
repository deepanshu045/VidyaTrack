import type { SVGProps } from "react";

export default function BrandMark({
  className = "",
  ...props
}: SVGProps<SVGSVGElement>) {
  return (
    <svg
      aria-hidden="true"
      className={className}
      fill="none"
      viewBox="0 0 48 48"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <rect fill="currentColor" height="48" rx="14" width="48" />
      <path
        d="M10.5 15.5c5.2-1.2 9.7-.3 13.5 2.7v17c-3.8-3-8.3-3.9-13.5-2.7v-17Z"
        fill="#D1FAE5"
      />
      <path
        d="M37.5 15.5c-5.2-1.2-9.7-.3-13.5 2.7v17c3.8-3 8.3-3.9 13.5-2.7v-17Z"
        fill="#FFFFFF"
      />
      <path d="m29 12.5 3 3 6-6" stroke="#FBBF24" strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" />
      <path d="M24 18.2v17" stroke="#047857" strokeLinecap="round" strokeWidth="1.5" />
    </svg>
  );
}
